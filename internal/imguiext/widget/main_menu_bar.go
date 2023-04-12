package widget

import "github.com/SpaiR/imgui-go"

type mainMenuBarWidget struct {
	layout Layout
}

func (m *mainMenuBarWidget) Build() {
	if imgui.BeginMainMenuBar() {
		if m.layout != nil {
			m.layout.Build()
		}
		imgui.EndMainMenuBar()
	}
}

func MainMenuBar(layout Layout) *mainMenuBarWidget {
	return &mainMenuBarWidget{
		layout: layout,
	}
}
