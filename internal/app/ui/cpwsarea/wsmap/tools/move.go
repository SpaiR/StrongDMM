package tools

import (
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/util"
)

// ToolPick can be used move a single object.
type ToolMove struct {
	tool
	instance *dmminstance.Instance
	lastTile *dmmap.Tile
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
	}
}

func (t *ToolMove) onMove(coord util.Point) {
	if t.instance != nil {
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
}

func (t *ToolMove) onStop(util.Point) {
	if t.instance != nil {
		//remove other turfs if we moved a turf
		if dm.IsPath(t.instance.Prefab().Path(), "/turf") {
			for _, found := range t.lastTile.Instances() {
				if dm.IsPath(found.Prefab().Path(), "/turf") && found != t.instance {
					ed.InstanceDelete(found)
				}
			}
		}
		t.instance = nil
		t.lastTile = nil
		go ed.CommitChanges("Moved Prefab")
	}
}
