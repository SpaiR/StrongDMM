package dmicon

import (
	"fmt"
	"image"
	"image/color"
	"image/draw"

	"github.com/SpaiR/strongdmm/internal/app/dm"
)

var Cache = &IconsCache{icons: make(map[string]*Dmi)}

type IconsCache struct {
	rootDirPath string
	icons       map[string]*Dmi
}

func (i *IconsCache) Free() {
	for _, dmi := range i.icons {
		dmi.free()
	}
	i.icons = make(map[string]*Dmi)
}

func (i *IconsCache) SetRootDirPath(rootDirPath string) {
	i.rootDirPath = rootDirPath
}

func (i *IconsCache) Get(icon string) (*Dmi, error) {
	if len(icon) == 0 {
		return nil, fmt.Errorf("dmi icon is empty")
	}

	if dmi, ok := i.icons[icon]; ok {
		if dmi == nil {
			return nil, fmt.Errorf("dmi [%s] is nil", icon)
		}
		return dmi, nil
	}

	dmi, err := New(i.rootDirPath + "/" + icon)
	i.icons[icon] = dmi
	return dmi, err
}

func (i *IconsCache) GetState(icon, state string) (*State, error) {
	dmi, err := i.Get(icon)
	if err != nil {
		return nil, err
	}
	return dmi.State(state)
}

func (i *IconsCache) GetSpriteV(icon, state string, dir int) (*Sprite, error) {
	dmiState, err := i.GetState(icon, state)
	if err != nil {
		return nil, err
	}
	return dmiState.SpriteV(dir), nil
}

func (i *IconsCache) GetSprite(icon, state string) (*Sprite, error) {
	return i.GetSpriteV(icon, state, dm.DirDefault)
}

var placeholder *Sprite

func (i *IconsCache) GetSpriteOrPlaceholder(icon, state string) *Sprite {
	return i.GetSpriteOrPlaceholderV(icon, state, dm.DirDefault)
}

func (i *IconsCache) GetSpriteOrPlaceholderV(icon, state string, dir int) *Sprite {
	if s, err := i.GetSpriteV(icon, state, dir); err == nil {
		return s
	}

	if placeholder == nil {
		placeholder = createPlaceholder()
	}

	return placeholder
}

func createPlaceholder() *Sprite {
	color1 := color.RGBA{R: 240, G: 214, B: 255, A: 200} // purple
	color2 := color.RGBA{R: 210, G: 105, B: 255, A: 200} // pink

	/*
		|1|2|
		|2|1|
	*/

	img := image.NewRGBA(image.Rect(0, 0, 2, 2))
	draw.Draw(img, image.Rect(0, 0, 1, 1), &image.Uniform{C: color1}, image.Point{}, draw.Src)
	draw.Draw(img, image.Rect(1, 1, 2, 2), &image.Uniform{C: color1}, image.Point{}, draw.Src)
	draw.Draw(img, image.Rect(1, 0, 2, 1), &image.Uniform{C: color2}, image.Point{}, draw.Src)
	draw.Draw(img, image.Rect(0, 1, 1, 2), &image.Uniform{C: color2}, image.Point{}, draw.Src)

	placeholderDmi := &Dmi{
		IconWidth:     32,
		IconHeight:    32,
		TextureWidth:  32,
		TextureHeight: 32,
		Cols:          1,
		Rows:          1,
		Texture:       createTexture(img),
	}

	return newDmiSprite(placeholderDmi, 0)
}
