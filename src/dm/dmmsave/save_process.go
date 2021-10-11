package dmmsave

import (
	"log"

	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmsave/keygen"
	"sdmm/util"
)

type saveProcess struct {
	dmm        *dmmap.Dmm
	initial    *dmmdata.DmmData
	output     *dmmdata.DmmData
	keygen     *keygen.KeyGen
	unusedKeys map[dmmdata.Key]bool
}

func makeSaveProcess(dmm *dmmap.Dmm, path string) (*saveProcess, error) {
	initial, err := dmmdata.New(dmm.Backup)
	if err != nil {
		log.Println("[dmmsave] unable to read map backup:", dmm.Backup)
		return nil, err
	}

	output := &dmmdata.DmmData{
		Filepath:   path,
		IsTgm:      initial.IsTgm,
		LineBreak:  initial.LineBreak,
		KeyLength:  initial.KeyLength,
		MaxX:       initial.MaxX,
		MaxY:       initial.MaxY,
		MaxZ:       initial.MaxZ,
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
		dmm,
		initial,
		output,
		keygen.New(output),
		unusedKeys,
	}, nil
}

// Go through the dmm tiles and try to find a key in the initial map with the same content.
func (sp *saveProcess) handleReusedKeys() {
	log.Println("[dmmsave] handle reused keys...")

	// InstanceCache the initial content, since we know it won't change.
	keyByContentCache := make(map[uint64]dmmdata.Key, len(sp.initial.Dictionary))
	for key, instances := range sp.initial.Dictionary {
		keyByContentCache[instances.Hash()] = key
	}

	for _, tile := range sp.dmm.Tiles {
		newContent := tile.Content().Sorted()
		if initialKey, ok := findKeyByTileContent(sp.initial, keyByContentCache, newContent); ok {
			sp.setOutputKeyContent(tile.Coord, initialKey, newContent)
			delete(sp.unusedKeys, initialKey)
		}
	}

	log.Println("[dmmsave] remaining count of unused keys:", len(sp.unusedKeys))
}

// Find all locations without keys and fill them with the content.
func (sp *saveProcess) handleLocationsWithoutKeys() error {
	log.Println("[dmmsave] handle locations without keys...")
	log.Println("[dmmsave] collecting locations without keys...")

	locsWithoutKey := sp.findLocationsWithoutKey()

	log.Println("[dmmsave] count of locations without keys:", len(locsWithoutKey))

	sp.tryToReuseKeysByTheirInitialLocation(locsWithoutKey)

	switch sp.fillLocations(locsWithoutKey) {
	case errorRegenerateKeys:
		return sp.handleLocationsWithoutKeys()
	case errorKeysLimitExceeded:
		return errorKeysLimitExceeded
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

	log.Println("[dmmsave] trying to match unused keys with its previous location...")

	// Copy to modify the original map safely during its iteration.
	unusedKeysCpy := make(map[dmmdata.Key]bool)
	for key := range sp.unusedKeys {
		unusedKeysCpy[key] = true
	}

	// Content can be the same for different locations, so we will remember an unusedKey we applied to locs.
	keyByContentCache := make(map[uint64]dmmdata.Key)

	for unusedKey := range unusedKeysCpy {
		for loc := range locsWithoutKey {
			content := sp.dmm.GetTile(loc).Content().Sorted()
			contentHash := content.Hash()

			// If the key was already applied to the content in a previous iteration.
			if cachedKey, ok := keyByContentCache[contentHash]; ok {
				sp.output.Grid[loc] = cachedKey
				continue
			}

			if sp.initial.Grid[loc] == unusedKey {
				keyByContentCache[contentHash] = unusedKey

				sp.setOutputKeyContent(loc, unusedKey, content)

				delete(sp.unusedKeys, unusedKey)
				delete(locsWithoutKey, loc)

				break
			}
		}
	}

	log.Println("[dmmsave] remaining count of unused keys:", len(sp.unusedKeys))
	log.Println("[dmmsave] count of locations without keys:", len(locsWithoutKey))
}

// File all locations without keys with the key and the content.
func (sp *saveProcess) fillLocations(locsWithoutKey map[util.Point]bool) error {
	log.Println("[dmmsave] handling remaining locations...")

	// For logs.
	var (
		reusedKeys  []dmmdata.Key
		createdKeys []dmmdata.Key
	)

	keyByContentCache := make(map[uint64]dmmdata.Key)

	for loc := range locsWithoutKey {
		content := sp.dmm.GetTile(loc).Content().Sorted()

		var key dmmdata.Key
		if reusableKey, ok := findKeyByTileContent(sp.output, keyByContentCache, content); ok {
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
					return errorKeysLimitExceeded
				}

				log.Println("[dmmsave] unable to create a key, changing key length:", newSize)

				sp.keygen.DropKeysPool()

				sp.output.KeyLength = newSize
				sp.output.Dictionary = make(dmmdata.DataDictionary)
				sp.output.Grid = make(dmmdata.DataGrid)
				sp.unusedKeys = nil

				return errorRegenerateKeys
			}
			createdKeys = append(createdKeys, key)
		}

		sp.setOutputKeyContent(loc, key, content)
	}

	log.Println("[dmmsave] all tiles handled")
	log.Println("[dmmsave] reused keys:", reusedKeys)
	log.Println("[dmmsave] created keys:", createdKeys)

	return nil
}

func (sp *saveProcess) setOutputKeyContent(loc util.Point, key dmmdata.Key, content dmmdata.Content) {
	sp.output.Grid[loc] = key
	sp.output.Dictionary[key] = content
}

func findKeyByTileContent(
	data *dmmdata.DmmData,
	keyByContentCache map[uint64]dmmdata.Key,
	content dmmdata.Content,
) (dmmdata.Key, bool) {
	contentHash := content.Hash()

	if key, ok := keyByContentCache[contentHash]; ok {
		return key, true
	}

	for key, instances := range data.Dictionary {
		if content.Equals(instances) {
			keyByContentCache[contentHash] = key
			return key, true
		}
	}

	return "", false
}
