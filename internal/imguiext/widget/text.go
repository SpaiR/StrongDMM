package widget

import "github.com/SpaiR/imgui-go"

type textWidget struct {
	text string
}

func (t *textWidget) Build() {
	imgui.Text(t.text)
}

func (t *textWidget) CalcSize() imgui.Vec2 {
	return imgui.CalcTextSize(t.text, true, -1)
}

func Text(text string) *textWidget {
	return &textWidget{
		text: text,
	}
}
