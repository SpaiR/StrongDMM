package tools

import (
	"math"

	"sdmm/util"
)

// Fill tool can be used to add prefabs to the map by filling the provided area.
// During mouse moving when the tool is active it will mark the area to fill.
// On stop the tool will fill the area a user has made.
//
// Default: obj place on top, area and turfs are replaced.
// Alternative: obj replaced, area and turfs are placed on top.
type tFill struct {
	tool

	editor editor

	start    util.Point
	fillArea util.Bounds
}

func (tFill) Name() string {
	return TNFill
}

func newFill(editor editor) *tFill {
	return &tFill{
		editor: editor,
	}
}

func (t *tFill) process() {
	if t.active() {
		if t.AltBehaviour() {
			t.editor.MarkEditedAreaV(t.fillArea, util.Color{}, overlayColorGold)
		} else {
			t.editor.MarkEditedArea(t.fillArea)
		}
	}
}

func (t *tFill) onStart(coord util.Point) {
	if _, ok := t.editor.SelectedPrefab(); ok {
		t.start = coord
		t.onMove(coord)
	}
}

func (t *tFill) onMove(coord util.Point) {
	if !t.active() {
		return
	}

	t.fillArea.X1 = float32(math.Min(float64(t.start.X), float64(coord.X)))
	t.fillArea.Y1 = float32(math.Min(float64(t.start.Y), float64(coord.Y)))
	t.fillArea.X2 = float32(math.Max(float64(t.start.X), float64(coord.X)))
	t.fillArea.Y2 = float32(math.Max(float64(t.start.Y), float64(coord.Y)))
}

func (t *tFill) onStop(util.Point) {
	if !t.active() {
		return
	}

	// Fill the area.
	if prefab, ok := t.editor.SelectedPrefab(); ok {
		for x := t.fillArea.X1; x <= t.fillArea.X2; x++ {
			for y := t.fillArea.Y1; y <= t.fillArea.Y2; y++ {
				coord := util.Point{X: int(x), Y: int(y), Z: t.start.Z}
				tile := t.editor.Dmm().GetTile(coord)
				t.basicPrefabAdd(tile, prefab)
			}
		}

		go t.editor.CommitChanges("Fill Atoms")
	}

	t.start = util.Point{}
	t.fillArea = util.Bounds{}
}

func (t *tFill) active() bool {
	return !t.start.Equals(0, 0, 0)
}
