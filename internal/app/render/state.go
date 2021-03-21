package render

type State struct {
	Scale  float32
	ShiftX float32
	ShiftY float32
}

func newState() *State {
	return &State{Scale: 1}
}

func (s *State) Translate(x, y float32) {
	s.ShiftX += x
	s.ShiftY += y
}

func (s *State) Zoom(zoomIn bool, scaleFactor float32) {
	if zoomIn {
		s.Scale *= scaleFactor
	} else {
		s.Scale /= scaleFactor
	}
}
