package canvas

import "github.com/SpaiR/imgui-go"

type Tools struct {
}

func NewTools() *Tools {
	return &Tools{}
}

func (t *Tools) Process() {
	imgui.Text("Tools")
}
