package widget

import "github.com/SpaiR/imgui-go"

type textDisabledWidget struct {
	text string
}

func (t *textDisabledWidget) Build() {
	imgui.TextDisabled(t.text)
}

func (t *textDisabledWidget) CalcSize() imgui.Vec2 {
	return imgui.CalcTextSize(t.text, true, -1)
}

func TextDisabled(text string) *textDisabledWidget {
	return &textDisabledWidget{
		text: text,
	}
}
