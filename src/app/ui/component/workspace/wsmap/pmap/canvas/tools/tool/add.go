package tool

import (
	"sdmm/dm"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/util"
)

type Modify interface {
	Dmm() *dmmap.Dmm
	UpdateCanvasByCoord(coord util.Point)
	SelectedInstance() (dmminstance.Instance, bool)
	CommitChanges(string)
}

// Add tool can be used to add instances to the map.
// During mouse moving when the tool is active a selected instance will be added on every tile under the mouse.
// You can't add the same instance twice on the same tile during the one OnStart -> OnStop cycle.
//
// Default: obj placed on top, area and turfs replaced.
// Alternative: obj replaced, area and turfs placed on top.
type Add struct {
	modify  Modify
	visuals Visuals

	// Objects will be replaced, turfs and areas will be added on top.
	altBehaviour bool

	tiles map[util.Point]bool
}

func NewAdd(modify Modify, visuals Visuals) *Add {
	return &Add{
		modify:  modify,
		visuals: visuals,
		tiles:   make(map[util.Point]bool),
	}
}

func (a *Add) OnStart(coord util.Point) {
	a.altBehaviour = isControlDown()
	a.OnMove(coord)
}

func (a *Add) OnMove(coord util.Point) {
	if instance, ok := a.modify.SelectedInstance(); ok && !a.tiles[coord] {
		a.tiles[coord] = true

		tile := a.modify.Dmm().GetTile(coord)

		if !a.altBehaviour {
			if dm.IsPath(instance.Path, "/area") {
				tile.RemoveInstancesByPath("/area")
			} else if dm.IsPath(instance.Path, "/turf") {
				tile.RemoveInstancesByPath("/turf")
			}
		} else if dm.IsPath(instance.Path, "/obj") {
			tile.RemoveInstancesByPath("/obj")
		}

		tile.AddInstance(instance)

		a.modify.UpdateCanvasByCoord(coord)
		a.visuals.MarkModifiedTile(coord)
	}
}

func (a *Add) OnStop(_ util.Point) {
	a.altBehaviour = false
	if len(a.tiles) != 0 {
		a.tiles = make(map[util.Point]bool, len(a.tiles))
		a.modify.CommitChanges("Add Atoms")
		a.visuals.ClearModifiedTiles()
	}
}
