package dmicon

import (
	"sdmm/assets"
	"sdmm/platform"
)

var (
	spritePlaceholder *Sprite
)

func initEditorSprites() {
	atlas := assets.EditorTextureAtlas()
	img := atlas.RGBA()

	dmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  atlas.Width,
		TextureHeight: atlas.Height,
		Cols:          1,
		Rows:          1,
		Image:         img,
		Texture:       platform.CreateTexture(img),
	}

	spritePlaceholder = newDmiSprite(dmi, 0)
}

func SpritePlaceholder() *Sprite {
	if spritePlaceholder == nil {
		initEditorSprites()
	}
	return spritePlaceholder
}
