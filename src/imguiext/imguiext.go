package imguiext

import "github.com/SpaiR/imgui-go"

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
