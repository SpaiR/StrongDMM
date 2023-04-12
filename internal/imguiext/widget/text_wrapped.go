package widget

import "github.com/SpaiR/imgui-go"

type textWrappedWidget struct {
	text string
}

func (t *textWrappedWidget) Build() {
	imgui.TextWrapped(t.text)
}

func TextWrapped(text string) *textWrappedWidget {
	return &textWrappedWidget{
		text: text,
	}
}
