package program

import (
	"image"
	"image/color"
	"image/draw"

	"github.com/SpaiR/strongdmm/pkg/platform"
)

var overlayTexture uint32

func OverlayTexture() uint32 {
	if overlayTexture == 0 {
		overlayTexture = createOverlayTexture()
	}
	return overlayTexture
}

func createOverlayTexture() uint32 {
	c := color.RGBA{R: 255, G: 255, B: 255, A: 255}
	img := image.NewRGBA(image.Rect(0, 0, 1, 1))
	draw.Draw(img, image.Rect(0, 0, 1, 1), &image.Uniform{C: c}, image.Point{}, draw.Src)
	return platform.CreateTexture(img)
}
