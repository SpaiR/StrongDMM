package tools

import (
	"math"

	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/overlay"

	"sdmm/internal/imguiext"
	"sdmm/internal/util"
)

// ToolFill can be used to add prefabs to the map by filling the provided area.
// During mouse moving when the tool is active it will mark the area to fill.
// On stop the tool will fill the area a user has made.
//
// Default: obj place on top, area and turfs are replaced.
// Alternative: obj replaced, area and turfs are placed on top.
type ToolFill struct {
	tool

	start    util.Point
	fillArea util.Bounds

	dragging bool
}

func (ToolFill) Name() string {
	return TNFill
}

func newFill() *ToolFill {
	return &ToolFill{}
}

func (t *ToolFill) Stale() bool {
	return !t.dragging
}

func (t *ToolFill) process() {
	if t.active() {
		if t.AltBehaviour() {
			ed.OverlayPushArea(t.fillArea, overlay.ColorToolFillAltTileFill, overlay.ColorToolFillAltTileBorder)
		} else {
			ed.OverlayPushArea(t.fillArea, overlay.ColorToolFillTileFill, overlay.ColorToolFillTileBorder)
		}
	}
}

func (t *ToolFill) onStart(coord util.Point) {
	if _, ok := ed.SelectedPrefab(); ok {
		t.dragging = true
		t.start = coord
		t.onMove(coord)
	}
}

func (t *ToolFill) onMove(coord util.Point) {
	if !t.active() {
		return
	}

	t.fillArea.X1 = float32(math.Min(float64(t.start.X), float64(coord.X)))
	t.fillArea.Y1 = float32(math.Min(float64(t.start.Y), float64(coord.Y)))
	t.fillArea.X2 = float32(math.Max(float64(t.start.X), float64(coord.X)))
	t.fillArea.Y2 = float32(math.Max(float64(t.start.Y), float64(coord.Y)))
}

func (t *ToolFill) onStop(util.Point) {
	if !t.active() {
		return
	}

	bordersOnly := imguiext.IsCtrlDown()
	// Fill the area.
	if prefab, ok := ed.SelectedPrefab(); ok {
		fillTile := func(x, y int) {
			coord := util.Point{X: x, Y: y, Z: t.start.Z}
			tile := ed.Dmm().GetTile(coord)
			t.basicPrefabAdd(tile, prefab)
		}
		if bordersOnly {
			rows := []float32{t.fillArea.Y1, t.fillArea.Y2}
			columns := []float32{t.fillArea.X1, t.fillArea.X2}
			for x := t.fillArea.X1; x <= t.fillArea.X2; x++ {
				for _, coordinate := range rows {
					fillTile(int(x), int(coordinate))
				}
			}
			for y := t.fillArea.Y1; y <= t.fillArea.Y2; y++ {
				for _, coordinate := range columns {
					fillTile(int(coordinate), int(y))
				}
			}
		} else {
			for x := t.fillArea.X1; x <= t.fillArea.X2; x++ {
				for y := t.fillArea.Y1; y <= t.fillArea.Y2; y++ {
					fillTile(int(x), int(y))
				}
			}
		}

		go ed.CommitChanges("Fill Atoms")
	}

	t.start = util.Point{}
	t.fillArea = util.Bounds{}

	t.dragging = false
}

func (t *ToolFill) active() bool {
	return !t.start.Equals(0, 0, 0)
}
