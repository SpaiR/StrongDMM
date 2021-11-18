package util

import "fmt"

// Bounds stores a 2D area bounds in float32 values.
type Bounds struct {
	X1, Y1 float32
	X2, Y2 float32
}

// Contains returns true if the current Bounds contains received point.
func (b Bounds) Contains(x, y float32) bool {
	return b.ContainsV(Bounds{x, y, x, y})
}

// ContainsV returns true if the current Bounds contains received area.
func (b Bounds) ContainsV(bounds Bounds) bool {
	return b.X2 >= bounds.X1 && b.Y2 >= bounds.Y1 && b.X1 <= bounds.X2 && b.Y1 <= bounds.Y2
}

func (b Bounds) String() string {
	return fmt.Sprintf("X1:%.0f, Y1:%.0f, X2:%.0f, Y2:%.0f", b.X1, b.Y1, b.X2, b.Y2)
}
