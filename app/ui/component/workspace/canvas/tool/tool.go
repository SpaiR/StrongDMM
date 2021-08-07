package tool

// Tool is a basic interface for tools in the panel.
type Tool interface {
	// OnStart goes when user clicks on the map.
	OnStart(x, y int)
	// OnMove goes when user clicked and, while holding the mouse button, move the mouse.
	OnMove(x, y int)
	// OnStop goes when user releases the mouse button.
	OnStop(x, y int)
}
