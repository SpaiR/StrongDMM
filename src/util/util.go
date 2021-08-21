package util

import "fmt"

// Point is a generic class to store a 3D point in space.
type Point struct {
	X, Y, Z int
}

func (p Point) String() string {
	return fmt.Sprintf("X:%d, Y:%d, Z:%d", p.X, p.Y, p.Z)
}

// Djb2 is hashing method implemented by spec: http://www.cse.yorku.ca/~oz/hash.html
func Djb2(str string) uint64 {
	var hash uint64 = 5381
	for _, c := range str {
		hash = ((hash << 5) + hash) + uint64(c)
	}
	return hash
}
