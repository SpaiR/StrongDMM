package bucket

type Bounds struct {
	X1, Y1 float32
	X2, Y2 float32
}

// Contains returns true if the current Bounds contain received coordinates.
func (b Bounds) Contains(x1, y1, x2, y2 float32) bool {
	return b.X2 >= x1 && b.Y2 >= y1 && b.X1 <= x2 && b.Y1 <= y2
}
