package canvas

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
)

type Status struct {
	coordX, coordY int
}

func NewStatus() *Status {
	return &Status{}
}

func (s *Status) Process() {
	if s.coordX == -1 || s.coordY == -1 {
		imgui.Text("[out of bounds]")
	} else {
		imgui.Text(fmt.Sprintf("[x:%03d y:%03d]", s.coordX, s.coordY))
	}
}

func (s *Status) UpdateCoords(coordX, coordY int) {
	s.coordX, s.coordY = coordX, coordY
}
