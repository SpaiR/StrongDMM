package tools

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
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

	editedTiles map[util.Point]bool
}

func (tAdd) Name() string {
	return TNAdd
}

func newAdd() *tAdd {
	return &tAdd{
		editedTiles: make(map[util.Point]bool),
	}
}

func (t *tAdd) process() {
	for coord := range t.editedTiles {
		if t.AltBehaviour() {
			ed.OverlayPushTile(coord, overlay.ColorToolAddAltTileFill, overlay.ColorToolAddAltTileBorder)
		} else {
			ed.OverlayPushTile(coord, overlay.ColorToolAddTileFill, overlay.ColorToolAddTileBorder)
		}
	}
}

func (t *tAdd) onStart(coord util.Point) {
	t.onMove(coord)
}

func (t *tAdd) onMove(coord util.Point) {
	if prefab, ok := ed.SelectedPrefab(); ok && !t.editedTiles[coord] {
		t.editedTiles[coord] = true // Don't add to the same tile twice

		tile := ed.Dmm().GetTile(coord)
		t.basicPrefabAdd(tile, prefab)

		ed.UpdateCanvasByCoords([]util.Point{coord})
	}
}

func (t *tAdd) onStop(util.Point) {
	if len(t.editedTiles) != 0 {
		t.editedTiles = make(map[util.Point]bool, len(t.editedTiles))
		go ed.CommitChanges("Add Atoms")
	}
}
