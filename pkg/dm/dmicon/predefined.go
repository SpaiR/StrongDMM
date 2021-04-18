package dmicon

import (
	"github.com/SpaiR/strongdmm/assets"
	"github.com/SpaiR/strongdmm/pkg/platform"
)

var placeholder *Sprite

func createPlaceholder() *Sprite {
	// TODO: world.icon_size
	return newDmiSprite(&Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  32,
		TextureHeight: 32,
		Cols:          1,
		Rows:          1,
		Texture:       platform.CreateTexture(assets.Placeholder.RGBA()),
	}, 0)
}

func SpritePlaceholder() *Sprite {
	if placeholder == nil {
		placeholder = createPlaceholder()
	}
	return placeholder
}
