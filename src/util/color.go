package util

import (
	"github.com/mazznoer/csscolorparser"
)

var (
	colorsCache map[string]csscolorparser.Color
)

func init() {
	colorsCache = make(map[string]csscolorparser.Color)
}

func ParseColor(color string) (float32, float32, float32, float32) {
	var c csscolorparser.Color
	if col, ok := colorsCache[color]; ok {
		c = col
	} else {
		if col, err := csscolorparser.Parse(color); err == nil {
			c = col
		} else {
			c = csscolorparser.Color{R: 1, G: 1, B: 1, A: 1}
		}
		colorsCache[color] = c
	}
	return float32(c.R), float32(c.G), float32(c.B), float32(c.A)
}
