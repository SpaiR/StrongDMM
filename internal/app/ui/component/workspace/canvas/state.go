package canvas

import (
	"github.com/SpaiR/strongdmm/pkg/util"
)

type State struct {
	MousePos    util.Point
	MousePosMap util.Point
}

func NewState() *State {
	return &State{
		MousePos:    util.Point{},
		MousePosMap: util.Point{},
	}
}
