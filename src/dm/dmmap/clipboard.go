package dmmap

import (
	"log"
	"sort"

	"sdmm/dm"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/util"
)

// Clipboard is a global storage for tiles to provide a copy/paste experience.
type Clipboard struct {
	buffer      []Tile
	pathsFilter *dm.PathsFilter
}

func NewClipboard(pathsFilter *dm.PathsFilter) *Clipboard {
	return &Clipboard{pathsFilter: pathsFilter}
}

func (c *Clipboard) Free() {
	c.buffer = nil
	log.Println("[dmmap] clipboard free")
}

func (c *Clipboard) Copy(dmm *Dmm, tiles []util.Point) {
	if len(tiles) == 0 {
		c.buffer = nil
		return
	}

	log.Printf("[dmmap] copy tiles to the clipboard buffer: %v", tiles)

	c.buffer = make([]Tile, 0, len(tiles))
	for _, pos := range tiles {
		if !dmm.HasTile(pos) {
			continue
		}

		tile := dmm.GetTile(pos).Copy()

		var tileContent dmmdata.Content
		for _, instance := range tile.Content() {
			if c.pathsFilter.IsVisiblePath(instance.Path()) {
				tileContent = append(tileContent, instance)
			}
		}

		tile.Set(tileContent)

		c.buffer = append(c.buffer, tile)
	}

	sort.Slice(c.buffer, func(i, j int) bool {
		return c.buffer[i].Coord.X < c.buffer[j].Coord.X
	})
}

func (c *Clipboard) Paste(dmm *Dmm, pastePos util.Point) {
	if len(c.buffer) == 0 {
		return
	}

	log.Printf("[dmmap] paste tiles from the clipboard buffer on the map: %v", pastePos)

	anchor := c.buffer[0].Coord
	for _, tileCopy := range c.buffer {
		pos := util.Point{
			X: pastePos.X + tileCopy.Coord.X - anchor.X,
			Y: pastePos.Y + tileCopy.Coord.Y - anchor.Y,
			Z: pastePos.Z,
		}

		if !dmm.HasTile(pos) {
			continue
		}

		tile := dmm.GetTile(pos)
		tile.Set(tileCopy.content.Copy())
		tile.AdjustBaseContent()
	}
}

func (c *Clipboard) HasData() bool {
	return len(c.buffer) != 0
}
