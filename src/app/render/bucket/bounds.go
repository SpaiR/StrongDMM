package bucket

import "fmt"

type Bounds struct {
	X1, Y1 float32
	X2, Y2 float32
}

// Contains returns true if the current Bounds contain received point.
func (b Bounds) Contains(x, y float32) bool {
	return b.ContainsV(x, y, x, y)
}

// ContainsV returns true if the current Bounds contain received area.
func (b Bounds) ContainsV(x1, y1, x2, y2 float32) bool {
	return b.X2 >= x1 && b.Y2 >= y1 && b.X1 <= x2 && b.Y1 <= y2
}

func (b Bounds) String() string {
	return fmt.Sprintf("X1:%.0f, Y1:%.0f, X2:%.0f, Y2:%.0f", b.X1, b.Y1, b.X2, b.Y2)
}
