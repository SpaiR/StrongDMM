package tool

import (
	"sdmm/util"
)

type AddAction interface {
	PMapHasSelectedInstance() bool
	PMapAddSelectedInstance(util.Point)
	PMapCommitChanges(string)
}

// Add tool can be used to add instances to the map.
// During mouse moving when the tool is active a selected instance will be added on every tile under the mouse.
// You can't add the same instance twice on the same tile during the one OnStart -> OnStop cycle.
type Add struct {
	action AddAction

	tiles map[util.Point]bool
}

func NewAdd(action AddAction) *Add {
	return &Add{
		action: action,
		tiles:  make(map[util.Point]bool),
	}
}

func (a *Add) OnStart(coord util.Point) {
	a.OnMove(coord)
}

func (a *Add) OnMove(coord util.Point) {
	if a.action.PMapHasSelectedInstance() && !a.tiles[coord] {
		a.tiles[coord] = true
		a.action.PMapAddSelectedInstance(coord)
	}
}

func (a *Add) OnStop(_ util.Point) {
	if len(a.tiles) != 0 {
		a.action.PMapCommitChanges("Add Tiles")
		a.tiles = make(map[util.Point]bool)
	}
}
