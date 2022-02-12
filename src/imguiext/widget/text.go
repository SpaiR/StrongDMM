package widget

import "github.com/SpaiR/imgui-go"

type textWidget struct {
	text string
}

func (t *textWidget) Build() {
	imgui.Text(t.text)
}

func Text(text string) *textWidget {
	return &textWidget{
		text: text,
	}
}
