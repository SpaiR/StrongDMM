package tool

import (
	"sdmm/util"
)

type Modify interface {
	HasSelectedInstance() bool
	AddSelectedInstance(util.Point)
	CommitChanges(string)
}

// Add tool can be used to add instances to the map.
// During mouse moving when the tool is active a selected instance will be added on every tile under the mouse.
// You can't add the same instance twice on the same tile during the one OnStart -> OnStop cycle.
type Add struct {
	modify  Modify
	visuals Visuals

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
	a.OnMove(coord)
}

func (a *Add) OnMove(coord util.Point) {
	if a.modify.HasSelectedInstance() && !a.tiles[coord] {
		a.tiles[coord] = true
		a.modify.AddSelectedInstance(coord)
		a.visuals.MarkModifiedTile(coord)
	}
}

func (a *Add) OnStop(_ util.Point) {
	if len(a.tiles) != 0 {
		a.tiles = make(map[util.Point]bool, len(a.tiles))
		a.modify.CommitChanges("Add Atoms")
		a.visuals.ClearModifiedTiles()
	}
}
