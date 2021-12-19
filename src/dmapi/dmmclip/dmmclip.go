package dmmclip

import (
	"log"
	"sort"

	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

type PasteData struct {
	Filter dm.PathsFilter
	Buffer []dmmap.Tile
}

// Clipboard is a global storage for tiles to provide a copy/paste experience.
type Clipboard struct {
	pasteData PasteData
}

func New() *Clipboard {
	return &Clipboard{}
}

func (c *Clipboard) Free() {
	c.pasteData = PasteData{}
	log.Println("[dmmclip] clipboard free")
}

func (c *Clipboard) Copy(pathsFilter *dm.PathsFilter, dmm *dmmap.Dmm, tiles []util.Point) {
	if len(tiles) == 0 {
		return
	}

	log.Printf("[dmmclip] copy tiles to the clipboard buffer: %v", tiles)

	c.pasteData.Filter = pathsFilter.Copy()
	c.pasteData.Buffer = make([]dmmap.Tile, 0, len(tiles))

	for _, pos := range tiles {
		if !dmm.HasTile(pos) {
			continue
		}

		tile := dmm.GetTile(pos).Copy()

		var prefabs dmmdata.Prefabs
		for _, instance := range tile.Instances() {
			if pathsFilter.IsVisiblePath(instance.Prefab().Path()) {
				prefabs = append(prefabs, instance.Prefab())
			}
		}

		tile.InstancesSet(prefabs)

		c.pasteData.Buffer = append(c.pasteData.Buffer, tile)
	}

	sort.Slice(c.pasteData.Buffer, func(i, j int) bool {
		return c.pasteData.Buffer[i].Coord.X < c.pasteData.Buffer[j].Coord.X
	})
}

func (c *Clipboard) Buffer() PasteData {
	return c.pasteData
}

func (c *Clipboard) HasData() bool {
	return len(c.pasteData.Buffer) != 0
}
