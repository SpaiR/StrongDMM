package imguiext

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
)

func SetItemHoveredTooltip(text string) {
	if imgui.IsItemHovered() {
		imgui.SetTooltip(text)
	}
}

func InputIntClamp(
	label string,
	v *int32,
	min, max, step, stepFast int,
) bool {
	if imgui.InputIntV(label, v, step, stepFast, imgui.InputTextFlagsNone) {
		if int(*v) > max {
			*v = int32(max)
		} else if int(*v) < min {
			*v = int32(min)
		}
		return true
	}
	return false
}

func IsAltDown() bool {
	return imgui.IsKeyDown(int(glfw.KeyLeftAlt)) || imgui.IsKeyDown(int(glfw.KeyRightAlt))
}

func IsShiftDown() bool {
	return imgui.IsKeyDown(int(glfw.KeyLeftShift)) || imgui.IsKeyDown(int(glfw.KeyRightShift))
}

func IsCtrlDown() bool {
	return imgui.IsKeyDown(int(glfw.KeyLeftControl)) || imgui.IsKeyDown(int(glfw.KeyRightControl))
}
