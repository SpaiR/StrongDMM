package dmmap

import (
	"fmt"
	"log"

	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

func (d *Dmm) Save() {
	d.SaveV(d.Path.Absolute)
}

func (d *Dmm) SaveV(path string) {
	logSaveDmm("start [" + path + "]...")

	initial, err := dmmdata.New(d.backup)
	if err != nil {
		log.Println("[dmmap] unable to read map backup:", d.backup)
		util.ShowErrorDialog("Unable to read map backup: " + d.backup)
		return
	}

	var (
		output = dmmdata.DmmData{
			Filepath:   path,
			IsTgm:      initial.IsTgm,
			LineBreak:  initial.LineBreak,
			KeyLength:  initial.KeyLength,
			MaxX:       initial.MaxX,
			MaxY:       initial.MaxY,
			MaxZ:       initial.MaxZ,
			Dictionary: make(map[dmmdata.Key][]dmminstance.Instance),
			Grid:       make(map[util.Point]dmmdata.Key),
		}

		unusedKeys   = initial.Keys()
		keyGenerator = newKeyGenerator(&output)
	)

	// Util function.
	removeUnusedKey := func(unusedKey dmmdata.Key) {
		for idx, key := range unusedKeys {
			if key == unusedKey {
				unusedKeys = append(unusedKeys[:idx], unusedKeys[idx+1:]...)
				break
			}
		}
	}

	// TODO: Add map sanitizing

	// Fill with reused keys.
	{
		logSaveDmm("filling with reused keys...")

		keyByContentCache := make(map[uint64]dmmdata.Key)

		for z := 1; z <= d.MaxZ; z++ {
			for y := 1; y <= d.MaxY; y++ {
				for x := 1; x <= d.MaxX; x++ {
					loc := util.Point{X: x, Y: y, Z: z}
					newContent := d.GetTile(loc).Content()

					if initialKey, ok := findKeyByTileContent(initial, keyByContentCache, newContent); ok {
						output.Grid[loc] = initialKey
						output.Dictionary[initialKey] = newContent
						removeUnusedKey(initialKey)
					}
				}
			}
		}

		logSaveDmm(fmt.Sprintf("remaining count of unused keys: %d", len(unusedKeys)))
	}

	// Fill remaining tiles.
	{
	fillRemainingTiles:
		logSaveDmm("filling remaining tiles...")
		logSaveDmm("collecting locations without keys...")

		locsWithoutKey := make(map[util.Point]bool)

		// Collect all locs without keys.
		for z := 1; z <= d.MaxZ; z++ {
			for y := 1; y <= d.MaxY; y++ {
				for x := 1; x <= d.MaxX; x++ {
					loc := util.Point{X: x, Y: y, Z: z}

					if _, ok := output.Grid[loc]; !ok {
						locsWithoutKey[loc] = true
					}
				}
			}
		}

		logSaveDmm(fmt.Sprintf("count of locations without keys: %d", len(locsWithoutKey)))

		// Try to find the most appropriate key for a location.
		if len(unusedKeys) != 0 {
			logSaveDmm("trying to match unused keys with its previous location...")

			// Copy to modify the original slice safely during its iteration.
			for _, unusedKey := range append(make([]dmmdata.Key, 0, len(unusedKeys)), unusedKeys...) {
				for loc := range locsWithoutKey {
					if initial.Grid[loc] == unusedKey {
						output.Grid[loc] = unusedKey
						output.Dictionary[unusedKey] = d.GetTile(loc).Content()

						removeUnusedKey(unusedKey)
						delete(locsWithoutKey, loc)

						break
					}
				}
			}

			logSaveDmm(fmt.Sprintf("remaining count of unused keys: %d", len(unusedKeys)))
			logSaveDmm(fmt.Sprintf("count of locations without keys: %d", len(locsWithoutKey)))
		}

		logSaveDmm("handling remaining locations...")

		keyByContentCache := make(map[uint64]dmmdata.Key)

		// For logging.
		var (
			reusedKeys  []dmmdata.Key
			createdKeys []dmmdata.Key
		)

		// Handle remaining locations.
		for loc := range locsWithoutKey {
			var key dmmdata.Key
			content := d.GetTile(loc).Content()

			if reusableKey, ok := findKeyByTileContent(&output, keyByContentCache, content); ok {
				key = reusableKey
			} else if len(unusedKeys) != 0 {
				key = unusedKeys[0]
				unusedKeys = append(unusedKeys[:0], unusedKeys[1:]...)
				reusedKeys = append(reusedKeys, key)
			} else {
				var newSize int
				if key, newSize = keyGenerator.createKey(); newSize != 0 {
					if newSize == -1 {
						util.ShowErrorDialog("Unable to save the map. Limit of keys exceeded.")
						return
					}

					logSaveDmm(fmt.Sprintf("unable to create a key, changing key length: %d", newSize))

					keyGenerator.dropKeysPool()

					output.KeyLength = newSize
					output.Dictionary = make(map[dmmdata.Key][]dmminstance.Instance)
					output.Grid = make(map[util.Point]dmmdata.Key)

					goto fillRemainingTiles
				}
				createdKeys = append(createdKeys, key)
			}

			output.Grid[loc] = key
			output.Dictionary[key] = content
		}

		logSaveDmm("all tiles handled")
		logSaveDmm(fmt.Sprintf("reused keys: %v", reusedKeys))
		logSaveDmm(fmt.Sprintf("created keys: %v", createdKeys))
	}

	logSaveDmm("saving dmm data...")

	output.Save()

	logSaveDmm("finish")
}

func logSaveDmm(msg string) {
	log.Println("[dmmap] SAVE DMM:", msg)
}

func findKeyByTileContent(
	data *dmmdata.DmmData,
	keyByContentCache map[uint64]dmmdata.Key,
	content TileContent,
) (dmmdata.Key, bool) {
	contentHash := content.Hash()

	if key, ok := keyByContentCache[contentHash]; ok {
		return key, true
	}

	for key, instances := range data.Dictionary {
		if TileContent(instances).Equals(content) {
			keyByContentCache[contentHash] = key
			return key, true
		}
	}

	return "", false
}
