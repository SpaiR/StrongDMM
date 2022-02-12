package dmicon

import (
	"sdmm/platform"
	"sdmm/rsc"
)

var (
	spritePlaceholder *Sprite
)

func initEditorSprites() {
	atlas := rsc.EditorTextureAtlas()
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
