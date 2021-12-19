package util

import "fmt"

// Point is a generic class to store a 3D point in space.
type Point struct {
	X, Y, Z int
}

func (p Point) Plus(point Point) (result Point) {
	result.X = p.X + point.X
	result.Y = p.Y + point.Y
	result.Z = p.Z + point.Z
	return result
}

func (p Point) Minus(point Point) (result Point) {
	result.X = p.X - point.X
	result.Y = p.Y - point.Y
	result.Z = p.Z - point.Z
	return result
}

func (p Point) Equals(x, y, z int) bool {
	return p.X == x && p.Y == y && p.Z == z
}

func (p Point) String() string {
	return fmt.Sprintf("X:%d, Y:%d, Z:%d", p.X, p.Y, p.Z)
}

func (p Point) Copy() Point {
	return Point{
		X: p.X,
		Y: p.Y,
		Z: p.Z,
	}
}
