package util

import (
	"github.com/mazznoer/csscolorparser"
)

type Color struct {
	r, g, b, a float32
}

func MakeColor(r float32, g float32, b float32, a float32) Color {
	return Color{r: r, g: g, b: b, a: a}
}

func (c Color) RGBA() (float32, float32, float32, float32) {
	return c.r, c.g, c.b, c.a
}

func (c Color) R() float32 {
	return c.r
}

func (c Color) G() float32 {
	return c.g
}

func (c Color) B() float32 {
	return c.b
}

func (c Color) A() float32 {
	return c.a
}

var (
	parsedColorsCache map[string]csscolorparser.Color
)

func init() {
	parsedColorsCache = make(map[string]csscolorparser.Color)
}

func ParseColor(color string) Color {
	var c csscolorparser.Color
	if col, ok := parsedColorsCache[color]; ok {
		c = col
	} else {
		if col, err := csscolorparser.Parse(color); err == nil {
			c = col
		} else {
			c = csscolorparser.Color{R: 1, G: 1, B: 1, A: 1}
		}
		parsedColorsCache[color] = c
	}
	return Color{
		r: float32(c.R),
		g: float32(c.G),
		b: float32(c.B),
		a: float32(c.A),
	}
}
