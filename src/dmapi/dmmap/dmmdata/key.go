package dmmdata

type Key string

const base = 52

var base52r map[rune]int

func init() {
	var base52 = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
	base52r = make(map[rune]int, len(base52))
	for idx, c := range base52 {
		base52r[c] = idx
	}
}

func (k Key) ToNum() int {
	num := 0
	for _, c := range k {
		num = base*num + base52r[c]
	}
	return num
}
