package widget

import "github.com/SpaiR/imgui-go"

type textDisabledWidget struct {
	text string
}

func (t *textDisabledWidget) Build() {
	imgui.TextDisabled(t.text)
}

func TextDisabled(text string) *textDisabledWidget {
	return &textDisabledWidget{
		text: text,
	}
}
