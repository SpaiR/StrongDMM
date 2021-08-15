package dmicon

import (
	"sdmm/assets"
	"sdmm/platform"
)

var (
	placeholder *Sprite
)

func initEditorSprites() {
	editorSpriteAtlas := assets.EditorSpriteAtlas()

	dmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  editorSpriteAtlas.Width,
		TextureHeight: editorSpriteAtlas.Height,
		Cols:          1,
		Rows:          1,
		Texture:       platform.CreateTexture(editorSpriteAtlas.RGBA()),
	}

	placeholder = newDmiSprite(dmi, 0)
}

func SpritePlaceholder() *Sprite {
	if placeholder == nil {
		initEditorSprites()
	}
	return placeholder
}
