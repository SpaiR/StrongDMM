package widget

import (
	"sdmm/internal/app/ui/shortcut"
	"sdmm/internal/imguiext/style"

	"github.com/SpaiR/imgui-go"
)

// Placeholder to check if there is an empty icon for the menuItem.
const miHolderEmptyIcon = "!/emptyIcon/!"

type menuItemWidget struct {
	label    string
	shortcut string
	selected bool
	enabled  bool
	icon     string
	onClick  func()
}

func (m *menuItemWidget) Shortcut(keys ...string) *menuItemWidget {
	m.shortcut = shortcut.Combine(keys...)
	return m
}

func (m *menuItemWidget) Selected(selected bool) *menuItemWidget {
	m.selected = selected
	return m
}

func (m *menuItemWidget) Enabled(enabled bool) *menuItemWidget {
	m.enabled = enabled
	return m
}

func (m *menuItemWidget) Icon(icon string) *menuItemWidget {
	m.icon = icon
	return m
}

func (m *menuItemWidget) IconEmpty() *menuItemWidget {
	m.icon = miHolderEmptyIcon
	return m
}

func (m *menuItemWidget) Build() {
	label := m.label

	var iconPos imgui.Vec2
	if len(m.icon) != 0 {
		// Add padding to the label text. This padding will be filled with the icon.
		label = "      " + label

		// Draw a dummy. It's needed to correctly process an icon padding.
		cursorPos := imgui.CursorPos()
		imgui.Dummy(imgui.Vec2{})
		imgui.SameLine()
		imgui.SetCursorPos(cursorPos)
		iconPos = imgui.ItemRectMin()
	}

	if imgui.MenuItemV(label, m.shortcut, m.selected, m.enabled) && m.onClick != nil {
		m.onClick()
	}

	if len(m.icon) != 0 && m.icon != miHolderEmptyIcon {
		var iconCol imgui.PackedColor
		if m.enabled {
			iconCol = style.ColorWhitePacked
		} else {
			iconCol = imgui.PackedColorFromVec4(imgui.CurrentStyle().Color(imgui.StyleColorTextDisabled))
		}
		imgui.WindowDrawList().AddText(iconPos, iconCol, m.icon)
	}
}

func MenuItem(label string, onClick func()) *menuItemWidget {
	return &menuItemWidget{
		label:   label,
		enabled: true,
		onClick: onClick,
	}
}
