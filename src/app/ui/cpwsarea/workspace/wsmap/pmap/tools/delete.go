package tools

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/util"
)

type tDelete struct {
	tool

	editor editor

	deletedTiles map[util.Point]bool
}

func (tDelete) Name() string {
	return TNDelete
}

func newDelete(editor editor) *tDelete {
	return &tDelete{
		editor:       editor,
		deletedTiles: make(map[util.Point]bool),
	}
}

func (t *tDelete) process() {
	for coord := range t.deletedTiles {
		if t.AltBehaviour() {
			t.editor.PushOverlayTile(coord, overlay.ColorToolDeleteAltTileFill, overlay.ColorToolDeleteAltTileBorder)
		}
	}
}

func (t *tDelete) onStart(coord util.Point) {
	if t.AltBehaviour() {
		t.onMove(coord)
	} else if hoveredInstance := t.editor.HoveredInstance(); hoveredInstance != nil {
		t.editor.DeleteInstance(hoveredInstance)
	}
}

func (t *tDelete) onMove(coord util.Point) {
	if t.AltBehaviour() && !t.deletedTiles[coord] {
		t.deletedTiles[coord] = true // Don't delete to the same tile twice
		t.editor.DeleteHoveredTile()
		t.editor.UpdateCanvasByCoords([]util.Point{coord})
	}
}

func (t *tDelete) onStop(util.Point) {
	if len(t.deletedTiles) != 0 {
		t.deletedTiles = make(map[util.Point]bool, len(t.deletedTiles))
		go t.editor.CommitChanges("Delete Tiles")
	}
}
