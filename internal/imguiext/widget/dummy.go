package widget

import "github.com/SpaiR/imgui-go"

type dummyWidget struct {
	size imgui.Vec2
}

func (s *dummyWidget) Build() {
	imgui.Dummy(s.size)
}

func Dummy(size imgui.Vec2) *dummyWidget {
	return &dummyWidget{
		size: size,
	}
}
