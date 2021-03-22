package canvas

import "github.com/SpaiR/imgui-go"

type Status struct {
}

func NewStatus() *Status {
	return &Status{}
}

func (s *Status) Process() {
	imgui.Text("Status")
}
