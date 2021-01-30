package dmicon

import (
	"fmt"
	"image"
	"image/color"
	"image/draw"

	"github.com/SpaiR/strongdmm/internal/app/byond"
)

var (
	RootDirPath string
	cache       map[string]*Dmi
)

func FreeCache() {
	for _, dmi := range cache {
		dmi.free()
	}
	cache = make(map[string]*Dmi)
}

func Get(icon string) (*Dmi, error) {
	if len(icon) == 0 {
		return nil, fmt.Errorf("dmi icon is empty")
	}

	if dmi, ok := cache[icon]; ok {
		if dmi == nil {
			return nil, fmt.Errorf("dmi [%s] is nil", icon)
		}
		return dmi, nil
	}

	dmi, err := New(RootDirPath + "/" + icon)
	cache[icon] = dmi
	return dmi, err
}

func GetState(icon, state string) (*State, error) {
	dmi, err := Get(icon)
	if err != nil {
		return nil, err
	}
	return dmi.State(state)
}

func GetSpriteD(icon, state string, dir int) (*Sprite, error) {
	dmiState, err := GetState(icon, state)
	if err != nil {
		return nil, err
	}
	return dmiState.SpriteD(dir), nil
}

func GetSprite(icon, state string) (*Sprite, error) {
	return GetSpriteD(icon, state, byond.DirDefault)
}

var placeholder *Sprite

func GetSpriteOrPlaceholder(icon, state string) *Sprite {
	if s, err := GetSprite(icon, state); err == nil {
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
