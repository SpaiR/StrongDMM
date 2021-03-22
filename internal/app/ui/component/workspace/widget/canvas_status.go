package widget

import "github.com/SpaiR/imgui-go"

type CanvasStatus struct {
}

func NewCanvasStatus() *CanvasStatus {
	return &CanvasStatus{}
}

func (c *CanvasStatus) Process() {
	imgui.Text("Status")
}
