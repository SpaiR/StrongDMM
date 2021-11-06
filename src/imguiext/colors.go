package imguiext

import (
	"image/color"

	"github.com/SpaiR/imgui-go"
)

var (
	ColorZero = imgui.Vec4{}

	ColorGold        = intHsl2col(51, 100, 50)
	ColorGoldLighter = intHsl2col(51, 100, 60)
	ColorGoldDarker  = intHsl2col(51, 100, 40)

	ColorRed        = intHsl2col(4, 60, 47)
	ColorRedLighter = intHsl2col(4, 60, 57)
	ColorRedDarker  = intHsl2col(4, 60, 37)

	ColorGreen1        = intHsl2col(112, 89, 28)
	ColorGreen1Lighter = intHsl2col(112, 89, 38)
	ColorGreen1Darker  = intHsl2col(112, 89, 18)

	ColorGreen2        = intHsl2col(109, 85, 41)
	ColorGreen2Lighter = intHsl2col(109, 85, 51)
	ColorGreen2Darker  = intHsl2col(109, 85, 31)

	ColorGreen3        = intHsl2col(103, 100, 49)
	ColorGreen3Lighter = intHsl2col(103, 100, 39)
	ColorGreen3Darker  = intHsl2col(103, 100, 59)

	ColorWhitePacked = imgui.Packed(color.RGBA{R: 255, G: 255, B: 255, A: 255})
)

func float2colV(r, g, b, a float32) imgui.Vec4 {
	return imgui.Vec4{X: r, Y: g, Z: b, W: a}
}

func intHsl2col(h, s, l int) imgui.Vec4 {
	return hsl2col(float32(h)/360, float32(s)/100, float32(l)/100)
}

func hsl2col(h, s, l float32) imgui.Vec4 {
	return hsl2colV(h, s, l, 1)
}

func hsl2colV(h, s, l, a float32) imgui.Vec4 {
	var q, p, r, g, b float32

	if s == 0 {
		r, g, b = l, l, l
	} else {
		if l < .5 {
			q = l * (1 + s)
		} else {
			q = l + s - l*s
		}
		p = 2*l - q
		r = hue2rgb(p, q, h+1.0/3)
		g = hue2rgb(p, q, h)
		b = hue2rgb(p, q, h-1.0/3)
	}

	return float2colV(r, g, b, a)
}

func hue2rgb(p, q, hue float32) float32 {
	h := hue
	if h < 0 {
		h += 1
	}
	if h > 1 {
		h -= 1
	}
	if 6*h < 1 {
		return p + ((q - p) * 6 * h)
	}
	if 2*h < 1 {
		return q
	}
	if 3*h < 2 {
		return p + ((q - p) * 6 * ((2.0 / 3.0) - h))
	}
	return p
}
