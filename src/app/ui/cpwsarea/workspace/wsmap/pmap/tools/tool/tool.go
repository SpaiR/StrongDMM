package tool

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/util"
)

// Tool is a basic interface for tools in the panel.
type Tool interface {
	// OnStart goes when user clicks on the map.
	OnStart(coord util.Point)
	// OnMove goes when user clicked and, while holding the mouse button, move the mouse.
	OnMove(coord util.Point)
	// OnStop goes when user releases the mouse button.
	OnStop(coord util.Point)
}

func isControlDown() bool {
	return imgui.IsKeyDown(int(glfw.KeyLeftControl)) || imgui.IsKeyDown(int(glfw.KeyRightControl))
}
