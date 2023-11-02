package dmicon

import (
	"sdmm/internal/platform"
	"sdmm/internal/rsc"
)

var (
	spritePlaceholder *Sprite
	whiteRect         *Sprite
)

func initEditorSprites() {
	atlas := rsc.EditorTextureAtlas()
	img := atlas.RGBA()

	dmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  atlas.Width,
		TextureHeight: atlas.Height,
		Cols:          2,
		Rows:          1,
		Image:         img,
		Texture:       platform.CreateTexture(img),
	}

	spritePlaceholder = newDmiSprite(dmi, 0)
	whiteRect = newDmiSprite(dmi, 1)
}

func SpritePlaceholder() *Sprite {
	if spritePlaceholder == nil {
		initEditorSprites()
	}
	return spritePlaceholder
}

func WhiteRect() *Sprite {
	if whiteRect == nil {
		initEditorSprites()
	}
	return whiteRect
}
