package canvas

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
)

type Status struct {
	state *State
}

func NewStatus(state *State) *Status {
	return &Status{
		state: state,
	}
}

func (s *Status) Process() {
	if s.state.MousePosMap.X == -1 || s.state.MousePosMap.Y == -1 {
		imgui.Text("[out of bounds]")
	} else {
		imgui.Text(fmt.Sprintf("[x:%03d y:%03d]", int(s.state.MousePosMap.X), int(s.state.MousePosMap.X)))
	}
}
