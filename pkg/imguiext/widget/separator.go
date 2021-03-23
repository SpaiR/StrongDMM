package widget

import "github.com/SpaiR/imgui-go"

type separatorWidget struct{}

func (s *separatorWidget) Build() {
	imgui.Separator()
}

func Separator() *separatorWidget {
	return &separatorWidget{}
}
