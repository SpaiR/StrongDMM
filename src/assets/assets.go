package assets

import (
	"bytes"
	_ "embed"
	"image"
	"image/draw"
	_ "image/png"
)

var (
	//go:embed font/Ruda-Bold.ttf
	fontTTF []byte
	//go:embed font/font-awesome-solid-900.ttf
	iconsTTF []byte
)

func FontTTF() []byte {
	return fontTTF
}

func IconsTTF() []byte {
	return iconsTTF
}

var (
	//go:embed png/editor.png
	editor []byte
)

var (
	// Editor is a png with all textures used by the editor.
	Editor = Asset{
		Width:  32,
		Height: 32,
		data:   editor,
	}
)

type Asset struct {
	Width  int
	Height int
	data   []byte
}

func (a Asset) RGBA() *image.RGBA {
	res, _, _ := image.Decode(bytes.NewReader(a.data))
	img := image.NewRGBA(image.Rect(0, 0, a.Width, a.Height))
	draw.Draw(img, img.Bounds(), res, image.Point{}, draw.Src)
	return img
}
