package widget

import "github.com/SpaiR/imgui-go"

type menuWidget struct {
	label   string
	enabled bool
	layout  Layout
}

func (m *menuWidget) Enabled(enabled bool) *menuWidget {
	m.enabled = enabled
	return m
}

func (m *menuWidget) Build() {
	if imgui.BeginMenuV(m.label, m.enabled) {
		if m.layout != nil {
			m.layout.Build()
		}
		imgui.EndMenu()
	}
}

func Menu(label string, layout Layout) *menuWidget {
	return &menuWidget{
		label:   label,
		enabled: true,
		layout:  layout,
	}
}
