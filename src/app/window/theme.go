package window

import "github.com/SpaiR/imgui-go"

func (w *Window) setDefaultTheme() {
	// TODO: Proper theming
	imgui.StyleColorsDark()

	s := imgui.CurrentStyle()
	s.SetWindowBorderSize(0)
	s.SetChildBorderSize(0)
	s.SetScrollbarSize(10)
}
