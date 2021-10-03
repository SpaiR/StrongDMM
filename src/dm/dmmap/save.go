package dmmap

import (
	"log"
	"strconv"
	"strings"

	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

func (d *Dmm) Save() {
	d.SaveV(d.Path.Absolute)
	log.Println("[dmmap] map saved:", d.Path.Absolute)
}

func (d *Dmm) SaveV(path string) {
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
		keyByContentCache := make(map[uint64]dmmdata.Key)

		for z := 1; z <= d.MaxZ; z++ {
			for y := 1; y <= d.MaxY; y++ {
				for x := 1; x <= d.MaxX; x++ {
					loc := util.Point{X: x, Y: y, Z: z}
					newContent := d.GetTile(loc).Content

					if initialKey, ok := findKeyByTileContent(initial, keyByContentCache, newContent); ok {
						output.Grid[loc] = initialKey
						output.Dictionary[initialKey] = copyContent(newContent)
						removeUnusedKey(initialKey)
					}
				}
			}
		}
	}

	// Fill remaining tiles.
	{
	fillRemainingTiles:
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

		// Try to find the most appropriate key for a location.
		if len(unusedKeys) != 0 {
			// Copy to modify the original slice safely during its iteration.
			for _, unusedKey := range append(make([]dmmdata.Key, 0, len(unusedKeys)), unusedKeys...) {
				for loc := range locsWithoutKey {
					if initial.Grid[loc] == unusedKey {
						output.Grid[loc] = unusedKey
						output.Dictionary[unusedKey] = copyContent(d.GetTile(loc).Content)

						removeUnusedKey(unusedKey)
						delete(locsWithoutKey, loc)

						break
					}
				}
			}
		}

		keyByContentCache := make(map[uint64]dmmdata.Key)

		// Handle remaining locations.
		for loc := range locsWithoutKey {
			var key dmmdata.Key
			content := d.GetTile(loc).Content

			if reusableKey, ok := findKeyByTileContent(&output, keyByContentCache, content); ok {
				key = reusableKey
			} else if len(unusedKeys) != 0 {
				key = unusedKeys[0]
				unusedKeys = append(unusedKeys[:0], unusedKeys[1:]...)
			} else {
				var newSize int
				if key, newSize = keyGenerator.createKey(); newSize != 0 {
					if newSize == -1 {
						util.ShowErrorDialog("Unable to save the map. Limit of keys exceeded.")
						return
					}

					output.KeyLength = newSize
					output.Dictionary = make(map[dmmdata.Key][]dmminstance.Instance)
					output.Grid = make(map[util.Point]dmmdata.Key)

					goto fillRemainingTiles
				}
			}

			output.Grid[loc] = key
			output.Dictionary[key] = copyContent(content)
		}
	}

	output.Save()
}

func calcContentHash(content []dmminstance.Instance) uint64 {
	sb := strings.Builder{}
	for _, instance := range content {
		sb.WriteString(strconv.FormatUint(instance.Id(), 10))
	}
	return util.Djb2(sb.String())
}

func findKeyByTileContent(
	data *dmmdata.DmmData,
	keyByContentCache map[uint64]dmmdata.Key,
	content []dmminstance.Instance) (dmmdata.Key, bool) {
	contentHash := calcContentHash(content)

	if key, ok := keyByContentCache[contentHash]; ok {
		return key, true
	}

	for key, instances := range data.Dictionary {
		if isSameContent(instances, content) {
			keyByContentCache[contentHash] = key
			return key, true
		}
	}

	return "", false
}

func isSameContent(cnt1, cnt2 []dmminstance.Instance) bool {
	if len(cnt1) != len(cnt2) {
		return false
	}

	for idx, instance1 := range cnt1 {
		if instance1.Id() != cnt2[idx].Id() {
			return false
		}
	}

	return true
}

func copyContent(content []dmminstance.Instance) []dmminstance.Instance {
	cpy := make([]dmminstance.Instance, 0, len(content))
	for _, instance := range content {
		cpy = append(cpy, instance)
	}
	return cpy
}
