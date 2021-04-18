package util

import "fmt"

type Point struct {
	X, Y float32
}

func (p Point) String() string {
	return fmt.Sprintf("X:%f, Y:%f", p.X, p.Y)
}
