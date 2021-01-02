package widget

import "github.com/SpaiR/imgui-go"

type menuItemWidget struct {
	label    string
	shortcut string
	selected bool
	enabled  bool
	onClick  func()
}

func (m *menuItemWidget) Shortcut(shortcut string) *menuItemWidget {
	m.shortcut = shortcut
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

func (m *menuItemWidget) Build() {
	if imgui.MenuItemV(m.label, m.shortcut, m.selected, m.enabled) && m.onClick != nil {
		m.onClick()
	}
}

func MenuItem(label string, onClick func()) *menuItemWidget {
	return &menuItemWidget{
		label:   label,
		enabled: true,
		onClick: onClick,
	}
}
