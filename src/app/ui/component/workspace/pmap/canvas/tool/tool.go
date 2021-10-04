package tool

import "sdmm/util"

type Action interface {
	ToolsAddModifiedTile(coord util.Point)
	ToolsResetModifiedTiles()
}

// Tool is a basic interface for tools in the panel.
type Tool interface {
	// OnStart goes when user clicks on the map.
	OnStart(coord util.Point)
	// OnMove goes when user clicked and, while holding the mouse button, move the mouse.
	OnMove(coord util.Point)
	// OnStop goes when user releases the mouse button.
	OnStop(coord util.Point)
}
