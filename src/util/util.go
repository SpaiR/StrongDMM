package util

// Point is a generic class to store two data value.
type Point struct {
	X, Y int
}

// Djb2 is hashing method implemented by spec: http://www.cse.yorku.ca/~oz/hash.html
func Djb2(str string) uint64 {
	var hash uint64 = 5381
	for _, c := range str {
		hash = ((hash << 5) + hash) + uint64(c)
	}
	return hash
}