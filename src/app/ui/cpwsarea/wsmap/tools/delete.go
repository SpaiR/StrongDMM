package tools

import (
	"sdmm/app/ui/cpwsarea/wsmap/pmap/overlay"
	"sdmm/util"
)

// ToolDelete can be used to delete a hovered object instance.
// It has an alt behaviour which is able to delete all instances on the tile.
type ToolDelete struct {
	tool

	deletedTiles map[util.Point]bool
}

func (ToolDelete) IgnoreBounds() bool {
	return true
}

func (ToolDelete) Name() string {
	return TNDelete
}

func newDelete() *ToolDelete {
	return &ToolDelete{
		deletedTiles: make(map[util.Point]bool),
	}
}

func (t *ToolDelete) process() {
	for coord := range t.deletedTiles {
		if t.AltBehaviour() {
			ed.OverlayPushTile(coord, overlay.ColorToolDeleteAltTileFill, overlay.ColorToolDeleteAltTileBorder)
		}
	}
}

func (t *ToolDelete) onStart(coord util.Point) {
	if t.AltBehaviour() {
		t.onMove(coord)
	} else if hoveredInstance := ed.HoveredInstance(); hoveredInstance != nil {
		ed.InstanceDelete(hoveredInstance)
		go ed.CommitChanges("Delete Instance")
	}
}

func (t *ToolDelete) onMove(coord util.Point) {
	if t.AltBehaviour() && !t.deletedTiles[coord] {
		t.deletedTiles[coord] = true // Don't delete to the same tile twice
		ed.TileDeleteSelected()
		ed.UpdateCanvasByCoords([]util.Point{coord})
	}
}

func (t *ToolDelete) onStop(util.Point) {
	if len(t.deletedTiles) != 0 {
		t.deletedTiles = make(map[util.Point]bool, len(t.deletedTiles))
		go ed.CommitChanges("Delete Tiles")
	}
}
