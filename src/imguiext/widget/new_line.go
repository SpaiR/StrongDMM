package widget

import "github.com/SpaiR/imgui-go"

type newLineWidget struct{}

func (s *newLineWidget) Build() {
	imgui.NewLine()
}

func NewLine() *newLineWidget {
	return &newLineWidget{}
}
