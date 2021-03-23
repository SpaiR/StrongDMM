package canvas

import "github.com/SpaiR/imgui-go"

type Tools struct {
}

func NewTools() *Tools {
	return &Tools{}
}

func (t *Tools) Process() {
	imgui.AlignTextToFramePadding()
	imgui.Text("Tools")
	imgui.SameLine()
	imgui.Button("Test")
}
