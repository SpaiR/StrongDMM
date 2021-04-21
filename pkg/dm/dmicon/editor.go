package dmicon

import (
	"github.com/SpaiR/strongdmm/assets"
	"github.com/SpaiR/strongdmm/pkg/platform"
)

var (
	overlayActive *Sprite
	placeholder   *Sprite
)

func initEditorSprites() {
	dmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  assets.Editor.Width,
		TextureHeight: assets.Editor.Height,
		Cols:          2,
		Rows:          1,
		Texture:       platform.CreateTexture(assets.Editor.RGBA()),
	}

	overlayActive = newDmiSprite(dmi, 0)
	placeholder = newDmiSprite(dmi, 1)
}

func SpriteOverlayActive() *Sprite {
	if overlayActive == nil {
		initEditorSprites()
	}
	return overlayActive
}

func SpritePlaceholder() *Sprite {
	if placeholder == nil {
		initEditorSprites()
	}
	return placeholder
}
