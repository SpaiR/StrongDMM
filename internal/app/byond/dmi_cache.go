package byond

import (
	"fmt"
	"image"
	"image/color"
	"image/draw"
)

var (
	DmiRootDirPath string
	dmiCache       map[string]*Dmi
)

func FreeDmiCache() {
	for _, dmi := range dmiCache {
		dmi.free()
	}
	dmiCache = make(map[string]*Dmi)
}

func GetDmi(icon string) (*Dmi, error) {
	if len(icon) == 0 {
		return nil, fmt.Errorf("dmi icon is empty")
	}

	if dmi, ok := dmiCache[icon]; ok {
		if dmi == nil {
			return nil, fmt.Errorf("dmi [%s] is nil", icon)
		}
		return dmi, nil
	}

	dmi, err := newDmi(DmiRootDirPath + "/" + icon)
	dmiCache[icon] = dmi
	return dmi, err
}

func GetDmiState(icon, state string) (*DmiState, error) {
	dmi, err := GetDmi(icon)
	if err != nil {
		return nil, err
	}
	return dmi.State(state)
}

func GetDmiSpriteD(icon, state string, dir int) (*DmiSprite, error) {
	dmiState, err := GetDmiState(icon, state)
	if err != nil {
		return nil, err
	}
	return dmiState.SpriteD(dir), nil
}

func GetDmiSprite(icon, state string) (*DmiSprite, error) {
	return GetDmiSpriteD(icon, state, DirDefault)
}

var placeholder *DmiSprite

func GetDmiSpriteOrPlaceholder(icon, state string) *DmiSprite {
	if s, err := GetDmiSprite(icon, state); err == nil {
		return s
	}

	if placeholder == nil {
		placeholder = createPlaceholder()
	}

	return placeholder
}

func createPlaceholder() *DmiSprite {
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
