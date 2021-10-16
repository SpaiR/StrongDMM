package dmicon

import (
	"sdmm/assets"
	"sdmm/platform"
)

var (
	placeholder *Sprite
)

func initEditorSprites() {
	editorTextureAtlas := assets.EditorTextureAtlas()

	dmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  editorTextureAtlas.Width,
		TextureHeight: editorTextureAtlas.Height,
		Cols:          1,
		Rows:          1,
		Texture:       platform.CreateTexture(editorTextureAtlas.RGBA()),
	}

	placeholder = newDmiSprite(dmi, 0)
}

func SpritePlaceholder() *Sprite {
	if placeholder == nil {
		initEditorSprites()
	}
	return placeholder
}
