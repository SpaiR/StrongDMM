package widget

import "github.com/SpaiR/imgui-go"

type separatorWidget struct{}

func (separatorWidget) Build() {
	imgui.Separator()
}

func Separator() *separatorWidget {
	return &separatorWidget{}
}
