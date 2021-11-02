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
	if s.state.HoverOutOfBounds() {
		imgui.Text("[out of bounds]")
	} else {
		imgui.Text(fmt.Sprintf("[X:%03d Y:%03d]", s.state.hoveredTile.X, s.state.hoveredTile.Y))
	}

	if s.state.hoveredInstance != nil {
		imgui.SameLine()
		imgui.Text(s.state.hoveredInstance.Prefab().Path())
	}
}
