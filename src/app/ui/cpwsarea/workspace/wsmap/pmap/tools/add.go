package tools

import (
	"sdmm/dmapi/dm"
	"sdmm/util"
)

// Add tool can be used to add prefabs to the map.
// During mouse moving when the tool is active a selected prefab will be added on every tile under the mouse.
// You can't add the same prefab twice on the same tile during the one OnStart -> OnStop cycle.
//
// Default: obj placed on top, area and turfs replaced.
// Alternative: obj replaced, area and turfs placed on top.
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

func (a *tAdd) onStart(coord util.Point) {
	a.onMove(coord)
}

func (a *tAdd) onMove(coord util.Point) {
	if prefab, ok := a.editor.SelectedPrefab(); ok && !a.editedTiles[coord] {
		a.editedTiles[coord] = true // Don't add to the same tile twice

		tile := a.editor.Dmm().GetTile(coord)

		if !a.altBehaviour {
			if dm.IsPath(prefab.Path(), "/area") {
				tile.InstancesRemoveByPath("/area")
			} else if dm.IsPath(prefab.Path(), "/turf") {
				tile.InstancesRemoveByPath("/turf")
			}
		} else if dm.IsPath(prefab.Path(), "/obj") {
			tile.InstancesRemoveByPath("/obj")
		}

		tile.InstancesAdd(prefab)
		tile.InstancesRegenerate()

		a.editor.UpdateCanvasByCoord(coord)
		a.editor.MarkEditedTile(coord)
	}
}

func (a *tAdd) onStop(_ util.Point) {
	a.altBehaviour = false
	if len(a.editedTiles) != 0 {
		a.editedTiles = make(map[util.Point]bool, len(a.editedTiles))
		a.editor.ClearEditedTiles()
		go a.editor.CommitChanges("Add Atoms")
	}
}
