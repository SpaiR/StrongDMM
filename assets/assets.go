package assets

import (
	"bytes"
	_ "embed"
	"image"
	"image/draw"
	_ "image/png"
)

//go:embed internal/placeholder.dmi
var placeholder []byte

var (
	// Placeholder is an icon used as a placeholder for sprites with no image.
	Placeholder = Asset{
		data:   placeholder,
		width:  2,
		height: 2,
	}
)

type Asset struct {
	data   []byte
	width  int
	height int
}

func (a Asset) RGBA() *image.RGBA {
	res, _, _ := image.Decode(bytes.NewReader(a.data))
	img := image.NewRGBA(image.Rect(0, 0, a.width, a.height))
	draw.Draw(img, img.Bounds(), res, image.Point{}, draw.Src)
	return img
}
