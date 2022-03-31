package widget

import "github.com/SpaiR/imgui-go"

type textColoredWidget struct {
	text  string
	color imgui.Vec4
}

func (t *textColoredWidget) Build() {
	imgui.TextColored(t.color, t.text)
}

func (t *textColoredWidget) CalcSize() imgui.Vec2 {
	return imgui.CalcTextSize(t.text, true, -1)
}

func TextColored(text string, color imgui.Vec4) *textColoredWidget {
	return &textColoredWidget{
		text:  text,
		color: color,
	}
}
