package widget

import "github.com/SpaiR/imgui-go"

type fontWidget struct {
	font    imgui.Font
	content Layout
}

func (w *fontWidget) Build() {
	imgui.PushFont(w.font)
	w.content.Build()
	imgui.PopFont()
}

func Font(font imgui.Font, content ...widget) *fontWidget {
	return &fontWidget{
		font:    font,
		content: content,
	}
}
