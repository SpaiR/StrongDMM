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
	addAction AddAction
	action    Action

	tiles map[util.Point]bool
}

func NewAdd(addAction AddAction, action Action) *Add {
	return &Add{
		addAction: addAction,
		action:    action,
		tiles:     make(map[util.Point]bool),
	}
}

func (a *Add) OnStart(coord util.Point) {
	a.OnMove(coord)
}

func (a *Add) OnMove(coord util.Point) {
	if a.addAction.PMapHasSelectedInstance() && !a.tiles[coord] {
		a.tiles[coord] = true
		a.addAction.PMapAddSelectedInstance(coord)
		a.action.ToolsAddModifiedTile(coord)
	}
}

func (a *Add) OnStop(_ util.Point) {
	if len(a.tiles) != 0 {
		a.tiles = make(map[util.Point]bool, len(a.tiles))
		a.addAction.PMapCommitChanges("Add Tiles")
		a.action.ToolsResetModifiedTiles()
	}
}
