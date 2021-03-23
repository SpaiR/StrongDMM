package layout

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
)

// phantomWindow is used to calculate content size.
// The window itself is fully unavailable for user: it's transparent, hidden under the docking layout and its content is disabled.
// Yes, it's the hack, but it's the only way to calculate content size to do a proper layout.
func phantomWindow(id string, content func()) {
	imgui.PushItemFlag(imgui.ItemFlagsDisabled, true)
	imgui.PushStyleVarFloat(imgui.StyleVarAlpha, 0)
	imgui.BeginV(fmt.Sprint("_phantom_", id), nil, imgui.WindowFlagsNoBringToFrontOnFocus|imgui.WindowFlagsNoMove|imgui.WindowFlagsNoDecoration)
	content()
	imgui.End()
	imgui.PopStyleVar()
	imgui.PopItemFlag()
}

// windowPadding is a shortcut to get current window padding from ImGui style.
func windowPadding() imgui.Vec2 {
	return imgui.CurrentStyle().WindowPadding()
}
