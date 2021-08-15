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

func (a *Add) OnStart(x, y int) {
	a.OnMove(x, y)
}

func (a *Add) OnMove(x, y int) {
	if a.action.PMapHasSelectedInstance() {
		pos := util.Point{X: x, Y: y, Z: 1} // TODO: respect Z-level
		if !a.tiles[pos] {
			a.tiles[pos] = true
			a.action.PMapAddSelectedInstance(pos)
		}
	}
}

func (a *Add) OnStop(_, _ int) {
	if len(a.tiles) != 0 {
		a.action.PMapCommitChanges("Add Tiles")
		a.tiles = make(map[util.Point]bool)
	}
}
