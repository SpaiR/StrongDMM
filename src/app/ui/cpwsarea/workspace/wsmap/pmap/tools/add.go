package tools

import (
	"sdmm/util"
)

// Add tool can be used to add prefabs to the map.
// During mouse moving when the tool is active a selected prefab will be added on every tile under the mouse.
// You can't add the same prefab twice on the same tile during the one OnStart -> OnStop cycle.
//
// Default: obj placed on top, area and turfs are replaced.
// Alternative: obj replaced, area and turfs are placed on top.
type tAdd struct {
	tool

	editor editor

	editedTiles map[util.Point]bool
}

func (tAdd) Name() string {
	return TNAdd
}

func newAdd(editor editor) *tAdd {
	return &tAdd{
		editor:      editor,
		editedTiles: make(map[util.Point]bool),
	}
}

func (t *tAdd) onStart(coord util.Point) {
	t.onMove(coord)
}

func (t *tAdd) onMove(coord util.Point) {
	if prefab, ok := t.editor.SelectedPrefab(); ok && !t.editedTiles[coord] {
		t.editedTiles[coord] = true // Don't add to the same tile twice

		tile := t.editor.Dmm().GetTile(coord)
		t.basicPrefabAdd(tile, prefab)

		t.editor.UpdateCanvasByCoord(coord)
		t.editor.MarkEditedTile(coord)
	}
}

func (t *tAdd) onStop(util.Point) {
	if len(t.editedTiles) != 0 {
		t.editedTiles = make(map[util.Point]bool, len(t.editedTiles))
		t.editor.ClearEditedTiles()
		go t.editor.CommitChanges("Add Atoms")
	}
}
