package widget

import "github.com/SpaiR/imgui-go"

type sameLineWidget struct{}

func (s *sameLineWidget) Build() {
	imgui.SameLine()
}

func SameLine() *sameLineWidget {
	return &sameLineWidget{}
}
