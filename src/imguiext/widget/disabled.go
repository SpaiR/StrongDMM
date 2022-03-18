package widget

import "github.com/SpaiR/imgui-go"

type disabledWidget struct {
	disabled bool
	layout   Layout
}

func (s *disabledWidget) Build() {
	imgui.BeginDisabledV(s.disabled)
	s.layout.Build()
	imgui.EndDisabled()
}

func (s *disabledWidget) CalcSize() (size imgui.Vec2) {
	return s.layout.CalcSize()
}

func Disabled(disabled bool, layout ...widget) *disabledWidget {
	return &disabledWidget{
		disabled,
		layout,
	}
}
