package tools

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/util"
)

type tDelete struct {
	tool

	deletedTiles map[util.Point]bool
}

func (tDelete) Name() string {
	return TNDelete
}

func newDelete() *tDelete {
	return &tDelete{
		deletedTiles: make(map[util.Point]bool),
	}
}

func (t *tDelete) process() {
	for coord := range t.deletedTiles {
		if t.AltBehaviour() {
			ed.OverlayPushTile(coord, overlay.ColorToolDeleteAltTileFill, overlay.ColorToolDeleteAltTileBorder)
		}
	}
}

func (t *tDelete) onStart(coord util.Point) {
	if t.AltBehaviour() {
		t.onMove(coord)
	} else if hoveredInstance := ed.HoveredInstance(); hoveredInstance != nil {
		ed.InstanceDelete(hoveredInstance)
		go ed.CommitChanges("Delete Instance")
	}
}

func (t *tDelete) onMove(coord util.Point) {
	if t.AltBehaviour() && !t.deletedTiles[coord] {
		t.deletedTiles[coord] = true // Don't delete to the same tile twice
		ed.TileDeleteHovered()
		ed.UpdateCanvasByCoords([]util.Point{coord})
	}
}

func (t *tDelete) onStop(util.Point) {
	if len(t.deletedTiles) != 0 {
		t.deletedTiles = make(map[util.Point]bool, len(t.deletedTiles))
		go ed.CommitChanges("Delete Tiles")
	}
}
