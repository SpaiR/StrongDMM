package dmmclip

import (
	"log"
	"sort"

	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

type pasteData struct {
	filter dm.PathsFilter
	buffer []dmmap.Tile
}

// Clipboard is a global storage for tiles to provide a copy/paste experience.
type Clipboard struct {
	pasteData pasteData
}

func New() *Clipboard {
	return &Clipboard{}
}

func (c *Clipboard) Free() {
	c.pasteData = pasteData{}
	log.Println("[dmmclip] clipboard free")
}

func (c *Clipboard) Copy(pathsFilter *dm.PathsFilter, dmm *dmmap.Dmm, tiles []util.Point) {
	if len(tiles) == 0 {
		return
	}

	log.Printf("[dmmclip] copy tiles to the clipboard buffer: %v", tiles)

	c.pasteData.filter = pathsFilter.Copy()
	c.pasteData.buffer = make([]dmmap.Tile, 0, len(tiles))

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

		c.pasteData.buffer = append(c.pasteData.buffer, tile)
	}

	sort.Slice(c.pasteData.buffer, func(i, j int) bool {
		return c.pasteData.buffer[i].Coord.X < c.pasteData.buffer[j].Coord.X
	})
}

func (c *Clipboard) Paste(dmm *dmmap.Dmm, pastePos util.Point) {
	if len(c.pasteData.buffer) == 0 {
		return
	}

	log.Printf("[dmmclip] paste tiles from the clipboard buffer on the map: %v", pastePos)

	anchor := c.pasteData.buffer[0].Coord

	for _, tileCopy := range c.pasteData.buffer {
		pos := util.Point{
			X: pastePos.X + tileCopy.Coord.X - anchor.X,
			Y: pastePos.Y + tileCopy.Coord.Y - anchor.Y,
			Z: pastePos.Z,
		}

		if !dmm.HasTile(pos) {
			continue
		}

		tile := dmm.GetTile(pos)
		currTilePrefabs := tile.Instances().Prefabs()
		newTilePrefabs := make(dmmdata.Prefabs, 0, len(currTilePrefabs))

		for _, prefab := range currTilePrefabs {
			if !c.pasteData.filter.IsVisiblePath(prefab.Path()) {
				newTilePrefabs = append(newTilePrefabs, prefab)
			}
		}
		newTilePrefabs = append(newTilePrefabs, tileCopy.Instances().Prefabs()...)

		tile.InstancesSet(newTilePrefabs.Sorted())
		tile.InstancesRegenerate()
	}
}

func (c *Clipboard) HasData() bool {
	return len(c.pasteData.buffer) != 0
}
