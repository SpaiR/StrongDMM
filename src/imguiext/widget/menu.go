package widget

import (
	"sdmm/imguiext/style"

	"github.com/SpaiR/imgui-go"
)

// Placeholder to check if there is an empty icon for the menu.
const mHolderEmptyIcon = "!/emptyIcon/!"

type menuWidget struct {
	label   string
	enabled bool
	icon    string
	layout  Layout
}

func (m *menuWidget) Enabled(enabled bool) *menuWidget {
	m.enabled = enabled
	return m
}

func (m *menuWidget) Icon(icon string) *menuWidget {
	m.icon = icon
	return m
}

func (m *menuWidget) IconEmpty() *menuWidget {
	m.icon = mHolderEmptyIcon
	return m
}

func (m *menuWidget) Build() {
	label := m.label

	var iconPos imgui.Vec2
	var iconCol imgui.PackedColor
	if len(m.icon) != 0 {
		// Add padding to the label text. This padding will be filled with the icon.
		label = "      " + label

		// Draw a dummy. It's needed to correctly process an icon padding.
		cursorPos := imgui.CursorPos()
		imgui.Dummy(imgui.Vec2{})
		imgui.SameLine()
		imgui.SetCursorPos(cursorPos)
		iconPos = imgui.ItemRectMin()

		if m.enabled {
			iconCol = style.ColorWhitePacked
		} else {
			iconCol = imgui.PackedColorFromVec4(imgui.CurrentStyle().Color(imgui.StyleColorTextDisabled))
		}
	}

	if imgui.BeginMenuV(label, m.enabled) {
		if m.layout != nil {
			m.layout.Build()
		}
		imgui.EndMenu()
	}

	if len(m.icon) != 0 && m.icon != miHolderEmptyIcon {
		imgui.WindowDrawList().AddText(iconPos, iconCol, m.icon)
	}
}

func Menu(label string, layout Layout) *menuWidget {
	return &menuWidget{
		label:   label,
		enabled: true,
		layout:  layout,
	}
}
