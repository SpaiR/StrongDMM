package tools

import (
	"sdmm/internal/app/prefs"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/imguiext"
	"sdmm/internal/util"
	"strconv"

	"github.com/SpaiR/imgui-go"
)

// ToolMove can be used move a single object.
type ToolMove struct {
	tool
	instance *dmminstance.Instance
	lastTile *dmmap.Tile
	lastMouseCoords imgui.Vec2
	lastOffsets [2]int
}

func (ToolMove) Name() string {
	return TNMove
}

func newMove() *ToolMove {
	return &ToolMove{}
}

func (t *ToolMove) Stale() bool {
	return t.instance == nil
}

func (ToolMove) AltBehaviour() bool {
	return false
}

func (t *ToolMove) onStart(util.Point) {
	if hoveredInstance := ed.HoveredInstance(); hoveredInstance != nil {
		ed.InstanceSelect(hoveredInstance)
		t.instance = hoveredInstance
		t.lastMouseCoords = imgui.MousePos()
		vars := t.instance.Prefab().Vars()
		if ed.Prefs().Editor.NudgeMode == prefs.SaveNudgeModePixel {
			t.lastOffsets = [2]int{vars.IntV("pixel_x", 0), vars.IntV("pixel_y", 0)}
		} else {
			t.lastOffsets = [2]int{vars.IntV("step_x", 0), vars.IntV("step_y", 0)}
		}
	}
}

func (t *ToolMove) process() {
	if t.instance == nil || !imguiext.IsShiftDown() {
		return
	}
	xAxis := "pixel_x"
	yAxis := "pixel_y"
	if ed.Prefs().Editor.NudgeMode == prefs.SaveNudgeModeStep {
		xAxis = "step_x"
		yAxis = "step_y"
	}
	origPrefab := t.instance.Prefab()
	mouseCoords := imgui.MousePos()
	offsetX :=  (mouseCoords.X - t.lastMouseCoords.X) / ed.ZoomLevel()
	offsetY :=  (t.lastMouseCoords.Y - mouseCoords.Y) / ed.ZoomLevel()

	newVars := dmvars.Set(origPrefab.Vars(), xAxis, strconv.Itoa(t.lastOffsets[0] + int(offsetX)))
	newVars = dmvars.Set(newVars, yAxis, strconv.Itoa(t.lastOffsets[1] + int(offsetY)))
	t.instance.SetPrefab(dmmprefab.New(dmmprefab.IdNone, origPrefab.Path(), newVars))

	ed.UpdateCanvasByCoords([]util.Point{t.instance.Coord()})
}

func (t *ToolMove) onMove(coord util.Point) {
	if(t.instance == nil || imguiext.IsShiftDown()) {
		return
	}

	prefab := t.instance.Prefab()
	if t.lastTile != nil {
		t.lastTile.InstancesRegenerate() //should stop some issues
	}
	t.lastTile = ed.Dmm().GetTile(coord)
	ed.InstanceDelete(t.instance)
	t.lastTile.InstancesAdd(prefab)
	t.lastTile.InstancesRegenerate()
	for _, found := range t.lastTile.Instances() {
		if found.Prefab().Id() == prefab.Id() {
			t.instance = found
			break
		}
	}
	ed.UpdateCanvasByCoords([]util.Point{coord})
}

func (t *ToolMove) onStop(util.Point) {
	if t.instance == nil {
		return
	}
	//remove other turfs if we moved a turf
	if t.lastTile != nil {
		if dm.IsPath(t.instance.Prefab().Path(), "/turf") {
			for _, found := range t.lastTile.Instances() {
				if dm.IsPath(found.Prefab().Path(), "/turf") && found != t.instance {
					ed.InstanceDelete(found)
				}
			}
		}
	}
	t.instance = nil
	t.lastTile = nil
	go ed.CommitChanges("Moved Prefab")
}
