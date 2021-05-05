package render

type Camera struct {
	Scale  float32
	ShiftX float32
	ShiftY float32
}

func (s *Camera) Translate(x, y float32) {
	s.ShiftX += x
	s.ShiftY += y
}

func (s *Camera) Zoom(zoomIn bool, scaleFactor float32) {
	if zoomIn {
		s.Scale *= scaleFactor
	} else {
		s.Scale /= scaleFactor
	}
}
