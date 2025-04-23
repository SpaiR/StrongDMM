package dmmsave

import (
	"errors"

	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmvars"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata"
	"sdmm/internal/dmapi/dmmsave/keygen"
	"sdmm/internal/util"

	"github.com/rs/zerolog/log"
)

type saveProcess struct {
	cfg        Config
	dme        *dmenv.Dme
	dmm        *dmmap.Dmm
	initial    *dmmdata.DmmData
	output     *dmmdata.DmmData
	keygen     *keygen.KeyGen
	unusedKeys map[dmmdata.Key]bool
}

func makeSaveProcess(cfg Config, dme *dmenv.Dme, dmm *dmmap.Dmm, path string) (*saveProcess, error) {
	// Copy the dmm to avoid unneeded modifications.
	dmmCopy := dmm.Copy()
	dmm = &dmmCopy

	initial, err := dmmdata.New(dmm.Backup)
	if err != nil {
		log.Print("unable to read map backup:", dmm.Backup)
		return nil, err
	}

	output := &dmmdata.DmmData{
		Filepath:   path,
		IsTgm:      detectIsTgm(cfg.Format, initial.IsTgm),
		LineBreak:  initial.LineBreak,
		KeyLength:  initial.KeyLength,
		MaxX:       dmm.MaxX,
		MaxY:       dmm.MaxY,
		MaxZ:       dmm.MaxZ,
		Dictionary: make(dmmdata.DataDictionary),
		Grid:       make(dmmdata.DataGrid),
	}

	// Collect unused keys in map.
	// Use map instead of slice, because during the first phase (fill with reused keys) it's modified a lot.
	unusedKeys := make(map[dmmdata.Key]bool)
	for _, key := range initial.Keys() {
		unusedKeys[key] = true
	}

	return &saveProcess{
		cfg,
		dme,
		dmm,
		initial,
		output,
		keygen.New(output),
		unusedKeys,
	}, nil
}

func detectIsTgm(saveFormat Format, isInitialTGM bool) bool {
	switch saveFormat {
	case FormatInitial:
		return isInitialTGM
	case FormatTGM:
		return true
	default:
		return false
	}
}

func (sp *saveProcess) sanitizeVariables() {
	log.Print("sanitizing variables...")

	for _, tile := range sp.dmm.Tiles {
		for _, instance := range tile.Instances() {
			prefab := instance.Prefab()
			if prefab.Vars().Len() == 0 {
				continue
			}

			obj := sp.dme.Objects[prefab.Path()]
			vars := prefab.Vars()

			for _, varName := range prefab.Vars().Iterate() {
				origValue, _ := obj.Vars.Value(varName)
				prefValue, _ := prefab.Vars().Value(varName)

				if origValue == prefValue {
					log.Print("delete variable:", varName)
					vars = dmvars.Delete(vars, varName)
				}
			}

			if prefab.Vars().Len() != vars.Len() {
				instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, prefab.Path(), vars, prefab.Location()))
				log.Printf("instance sanitized: [%d#%s]", instance.Id(), prefab.Path())
			}
		}
	}
}

// Go through the dmm tiles and try to find a key in the initial map with the same content.
func (sp *saveProcess) handleReusedKeys() {
	log.Print("handle reused keys...")

	// Cache the initial content, since we know it won't change.
	keyByPrefabs := make(map[uint64]dmmdata.Key, len(sp.initial.Dictionary))
	for key, prefabs := range sp.initial.Dictionary {
		keyByPrefabs[prefabs.Hash()] = key
	}

	for _, tile := range sp.dmm.Tiles {
		prefabs := tile.Instances().Sorted().Prefabs()
		if initialKey, ok := findKeyByTileContent(sp.initial, keyByPrefabs, prefabs); ok {
			sp.setOutputKeyContent(tile.Coord, initialKey, prefabs)
			delete(sp.unusedKeys, initialKey)
		}
	}

	log.Print("remaining count of unused keys:", len(sp.unusedKeys))
}

// Find all locations without keys and fill them with the content.
func (sp *saveProcess) handleLocationsWithoutKeys() error {
	log.Print("handle locations without keys...")
	log.Print("collecting locations without keys...")

	locsWithoutKey := sp.findLocationsWithoutKey()

	log.Print("count of locations without keys:", len(locsWithoutKey))

	sp.tryToReuseKeysByTheirInitialLocation(locsWithoutKey)

	err := sp.fillLocations(locsWithoutKey)
	if errors.Is(err, errRegenerateKeys) {
		sp.keygen.DropKeysPool()
		sp.output.Dictionary = make(dmmdata.DataDictionary)
		sp.output.Grid = make(dmmdata.DataGrid)
		sp.unusedKeys = nil
		return sp.handleLocationsWithoutKeys()
	} else if errors.Is(err, errKeysLimitExceeded) {
		return errKeysLimitExceeded
	}

	return nil
}

func (sp *saveProcess) findLocationsWithoutKey() map[util.Point]bool {
	locsWithoutKey := make(map[util.Point]bool)

	for z := 1; z <= sp.dmm.MaxZ; z++ {
		for y := 1; y <= sp.dmm.MaxY; y++ {
			for x := 1; x <= sp.dmm.MaxX; x++ {
				loc := util.Point{X: x, Y: y, Z: z}
				if _, ok := sp.output.Grid[loc]; !ok {
					locsWithoutKey[loc] = true
				}
			}
		}
	}

	return locsWithoutKey
}

// Try to find the most appropriate place of all unused keys.
// Appropriate means that the initial map has the same key by the same location.
func (sp *saveProcess) tryToReuseKeysByTheirInitialLocation(locsWithoutKey map[util.Point]bool) {
	if len(sp.unusedKeys) == 0 {
		return
	}

	log.Print("trying to match unused keys with its previous location...")

	// Copy to modify the original map safely during its iteration.
	unusedKeysCpy := make(map[dmmdata.Key]bool)
	for key := range sp.unusedKeys {
		unusedKeysCpy[key] = true
	}

	// Content can be the same for different locations, so we will remember an unusedKey we applied to locs.
	keyByPrefabs := make(map[uint64]dmmdata.Key)

	for unusedKey := range unusedKeysCpy {
		for loc := range locsWithoutKey {
			prefabs := sp.dmm.GetTile(loc).Instances().Sorted().Prefabs()
			prefabsHash := prefabs.Hash()

			// If the key was already applied to the content in a previous iteration.
			if cachedKey, ok := keyByPrefabs[prefabsHash]; ok {
				sp.output.Grid[loc] = cachedKey
				continue
			}

			if sp.initial.Grid[loc] == unusedKey {
				keyByPrefabs[prefabsHash] = unusedKey

				sp.setOutputKeyContent(loc, unusedKey, prefabs)

				delete(sp.unusedKeys, unusedKey)
				delete(locsWithoutKey, loc)

				break
			}
		}
	}

	log.Print("remaining count of unused keys:", len(sp.unusedKeys))
	log.Print("count of locations without keys:", len(locsWithoutKey))
}

// File all locations without keys with the key and the content.
func (sp *saveProcess) fillLocations(locsWithoutKey map[util.Point]bool) error {
	log.Print("handling remaining locations...")

	// For logs.
	var (
		reusedKeys  []dmmdata.Key
		createdKeys []dmmdata.Key
	)

	keyByPrefabs := make(map[uint64]dmmdata.Key)

	for loc := range locsWithoutKey {
		prefabs := sp.dmm.GetTile(loc).Instances().Sorted().Prefabs()

		var key dmmdata.Key
		if reusableKey, ok := findKeyByTileContent(sp.output, keyByPrefabs, prefabs); ok {
			key = reusableKey
		} else if len(sp.unusedKeys) != 0 {
			for unusedKey := range sp.unusedKeys { // Pick up the first available key.
				key = unusedKey
				delete(sp.unusedKeys, unusedKey)
				reusedKeys = append(reusedKeys, key)
				break
			}
		} else {
			var newSize int
			if key, newSize = sp.keygen.CreateKey(); newSize != 0 {
				if newSize == -1 {
					return errKeysLimitExceeded
				}
				sp.output.KeyLength = newSize
				log.Print("unable to create a key, changing key length:", newSize)
				return errRegenerateKeys
			}
			createdKeys = append(createdKeys, key)
		}

		sp.setOutputKeyContent(loc, key, prefabs)
	}

	log.Print("all tiles handled")
	log.Print("reused keys:", reusedKeys)
	log.Print("created keys:", createdKeys)

	return nil
}

func (sp *saveProcess) setOutputKeyContent(loc util.Point, key dmmdata.Key, prefabs dmmdata.Prefabs) {
	sp.output.Grid[loc] = key
	sp.output.Dictionary[key] = prefabs
}

func findKeyByTileContent(
	data *dmmdata.DmmData,
	keyByPrefabs map[uint64]dmmdata.Key,
	prefabs dmmdata.Prefabs,
) (dmmdata.Key, bool) {
	contentHash := prefabs.Hash()

	if key, ok := keyByPrefabs[contentHash]; ok {
		return key, true
	}

	for key, dataContent := range data.Dictionary {
		if prefabs.Equals(dataContent) {
			keyByPrefabs[contentHash] = key
			return key, true
		}
	}

	return "", false
}
