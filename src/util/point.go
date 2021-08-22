package util

import "fmt"

// Point is a generic class to store a 3D point in space.
type Point struct {
	X, Y, Z int
}

func (p Point) String() string {
	return fmt.Sprintf("X:%d, Y:%d, Z:%d", p.X, p.Y, p.Z)
}
