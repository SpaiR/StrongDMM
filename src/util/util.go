package util

import (
	"github.com/sqweek/dialog"
	"image"
	"image/color"
)

// Djb2 is hashing method implemented by spec: http://www.cse.yorku.ca/~oz/hash.html
func Djb2(str string) uint64 {
	var hash uint64 = 5381
	for _, c := range str {
		hash = ((hash << 5) + hash) + uint64(c)
	}
	return hash
}

// ShowErrorDialog shows system error dialog to the user.
// Accepts dialog message to show.
func ShowErrorDialog(msg string) {
	ShowErrorDialogV("", msg)
}

// ShowErrorDialogV shows system error dialog to the user.
// Accepts dialog title and message to show.
func ShowErrorDialogV(title, msg string) {
	b := dialog.MsgBuilder{Msg: msg}
	b.Title(title)
	b.Error()
}

// PixelsToRGBA creates an RGBA image from provided raw pixels.
func PixelsToRGBA(pixels []byte, w, h int) *image.RGBA {
	img := image.NewRGBA(image.Rect(0, 0, w, h))

	for x := 0; x < w; x++ {
		for y := 0; y < h; y++ {
			pos := 4 * ((h-1-y)*w + x)
			r := pixels[pos] & 0xff
			g := pixels[pos+1] & 0xff
			b := pixels[pos+2] & 0xff
			a := pixels[pos+3] & 0xff
			if a != 0 {
				img.Set(x, y, color.RGBA{R: r, G: g, B: b, A: a})
			}
		}
	}

	return img
}
