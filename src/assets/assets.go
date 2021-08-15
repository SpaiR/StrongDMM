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
	fontIconsTTF []byte
)

func FontTTF() []byte {
	return fontTTF
}

func FontIconsTTF() []byte {
	return fontIconsTTF
}

var (
	//go:embed png/editor_texture_atlas.png
	editorSpriteAtlas []byte
)

// EditorTextureAtlas returns a sprite atlas with all textures used by the editor.
func EditorTextureAtlas() TextureAtlas {
	return TextureAtlas{
		Width:  32,
		Height: 32,
		data:   editorSpriteAtlas,
	}
}

type TextureAtlas struct {
	Width  int
	Height int
	data   []byte
}

func (a TextureAtlas) RGBA() *image.RGBA {
	res, _, _ := image.Decode(bytes.NewReader(a.data))
	img := image.NewRGBA(image.Rect(0, 0, a.Width, a.Height))
	draw.Draw(img, img.Bounds(), res, image.Point{}, draw.Src)
	return img
}
