package widget

import "github.com/SpaiR/imgui-go"

type CanvasTools struct {
}

func NewCanvasTools() *CanvasTools {
	return &CanvasTools{}
}

func (c *CanvasTools) Process() {
	imgui.Text("Tools")
}
