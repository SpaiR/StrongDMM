package tools

import "sdmm/util"

type tDelete struct {
	tool

	editor editor

	editedTiles map[util.Point]bool
}

func (tDelete) Name() string {
	return TNDelete
}

func newDelete(editor editor) *tDelete {
	return &tDelete{
		editor:      editor,
		editedTiles: make(map[util.Point]bool),
	}
}

func (t *tDelete) process() {
	for coord := range t.editedTiles {
		t.editor.MarkDeletedTile(coord)
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
	if t.AltBehaviour() && !t.editedTiles[coord] {
		t.editedTiles[coord] = true // Don't delete to the same tile twice

		t.editor.DeleteHoveredTile(false)

		t.editor.UpdateCanvasByCoord(coord)
		t.editor.MarkDeletedTile(coord)
	}
}

func (t *tDelete) onStop(util.Point) {
	if len(t.editedTiles) != 0 {
		t.editedTiles = make(map[util.Point]bool, len(t.editedTiles))
		go t.editor.CommitChanges("Delete Tiles")
	}
}
