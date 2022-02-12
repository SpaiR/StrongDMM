package rsc

import (
	"bytes"
	_ "embed"
	"image"
	"image/draw"
	_ "image/png"
	"log"
)

var (
	//go:embed png/editor_texture_atlas.png
	editorSpriteAtlas []byte
	//go:embed png/editor_icon.png
	editorIcon []byte
)

// EditorTextureAtlas returns a sprite atlas with all textures used by the editor.
func EditorTextureAtlas() TextureAtlas {
	return TextureAtlas{
		Width:  32,
		Height: 32,
		data:   editorSpriteAtlas,
	}
}

func EditorIcon() TextureAtlas {
	return TextureAtlas{
		Width:  1000,
		Height: 1000,
		data:   editorIcon,
	}
}

type TextureAtlas struct {
	Width  int
	Height int
	data   []byte
}

func (a TextureAtlas) RGBA() *image.NRGBA {
	res, _, err := image.Decode(bytes.NewReader(a.data))
	if err != nil {
		log.Panic("[assets] unable to decode texture atlas!")
	}
	img := image.NewNRGBA(image.Rect(0, 0, a.Width, a.Height))
	draw.Draw(img, img.Bounds(), res, image.Point{}, draw.Src)
	return img
}
