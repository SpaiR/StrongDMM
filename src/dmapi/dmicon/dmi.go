package dmicon

import (
	"fmt"
	"image"
	"image/draw"
	_ "image/png"
	"log"
	"os"

	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/dmapi/dm"
	"sdmm/platform"
	"sdmm/third_party/sdmmparser"
)

type Dmi struct {
	IconWidth     int
	IconHeight    int
	TextureWidth  int
	TextureHeight int
	Cols, Rows    int
	Image         image.Image
	Texture       uint32
	States        map[string]*State
}

func (d *Dmi) free() {
	gl.DeleteTextures(1, &d.Texture)
}

func (d *Dmi) State(state string) (*State, error) {
	if dmiState, ok := d.States[state]; ok {
		return dmiState, nil
	}
	if dmiState, ok := d.States[""]; ok {
		return dmiState, nil
	}
	return nil, fmt.Errorf("no dmi state by name [%s]", state)
}

func New(path string) (*Dmi, error) {
	iconMetadata, err := sdmmparser.ParseIconMetadata(path)
	if err != nil {
		log.Printf("[dmicon] unable to parse icon metadata [%s]: %s", path, err)
		return nil, err
	}

	f, err := os.Open(path)
	if err != nil {
		log.Printf("[dmicon] unable to open image file [%s]: %s", path, err)
		return nil, err
	}
	defer f.Close()

	imgOs, _, err := image.Decode(f)
	if err != nil {
		log.Printf("[dmicon] unable to decode image file [%s]: %s", path, err)
		return nil, err
	}

	rgba := image.NewNRGBA(imgOs.Bounds())
	draw.Draw(rgba, rgba.Bounds(), imgOs, image.Pt(0, 0), draw.Src)
	if rgba.Stride != rgba.Rect.Size().X*4 {
		return nil, fmt.Errorf("[dmicon] unable to convert image to NRGBA")
	}

	width := rgba.Bounds().Dx()
	height := rgba.Bounds().Dy()

	dmi := &Dmi{
		IconWidth:     iconMetadata.Width,
		IconHeight:    iconMetadata.Height,
		TextureWidth:  width,
		TextureHeight: height,
		Cols:          width / iconMetadata.Width,
		Rows:          height / iconMetadata.Height,
		Image:         rgba,
		Texture:       platform.CreateTexture(rgba),
		States:        make(map[string]*State),
	}

	spriteIdx := 0

	for _, state := range iconMetadata.States {
		dmiState := &State{
			Dirs:   state.Dirs,
			Frames: state.Frames,
		}

		for i := 0; i < state.Dirs*state.Frames; i++ {
			dmiState.Sprites = append(dmiState.Sprites, newDmiSprite(dmi, spriteIdx))
			spriteIdx += 1
		}

		dmi.States[state.Name] = dmiState
	}

	return dmi, nil
}

type State struct {
	Dirs, Frames int
	Sprites      []*Sprite
}

func (d State) Sprite() *Sprite {
	return d.SpriteV(dm.DirDefault)
}

func (d State) SpriteV(dir int) *Sprite {
	return d.SpriteByFrame(dir, 0)
}

func (d State) SpriteByFrame(dir, frame int) *Sprite {
	return d.Sprites[d.dir2idx(dir)+frame%d.Frames*d.Dirs]
}

func (d State) dir2idx(dir int) int {
	if d.Dirs == 1 || dir < dm.DirNorth || dir > dm.DirSouthwest {
		return 0
	}

	idx := 0
	switch dir {
	case dm.DirSouth:
		idx = 0
	case dm.DirNorth:
		idx = 1
	case dm.DirEast:
		idx = 2
	case dm.DirWest:
		idx = 3
	case dm.DirSoutheast:
		idx = 4
	case dm.DirSouthwest:
		idx = 5
	case dm.DirNortheast:
		idx = 6
	case dm.DirNorthwest:
		idx = 7
	}

	if idx+1 <= len(d.Sprites) {
		return idx
	}
	return 0
}

type Sprite struct {
	dmi            *Dmi
	X1, Y1, X2, Y2 int
	U1, V1, U2, V2 float32
}

func (d Sprite) Image() image.Image {
	return d.dmi.Image
}

func (d Sprite) Texture() uint32 {
	return d.dmi.Texture
}

func (d Sprite) TextureWidth() int {
	return d.dmi.TextureWidth
}

func (d Sprite) TextureHeight() int {
	return d.dmi.TextureHeight
}

func (d Sprite) IconWidth() int {
	return d.dmi.IconWidth
}

func (d Sprite) IconHeight() int {
	return d.dmi.IconHeight
}

func newDmiSprite(dmi *Dmi, idx int) *Sprite {
	const uvMargin = .000001
	x := idx % dmi.Cols
	y := idx / dmi.Cols
	return &Sprite{
		dmi: dmi,
		X1:  x * dmi.IconWidth,
		Y1:  y * dmi.IconHeight,
		X2:  (x + 1) * dmi.IconWidth,
		Y2:  (y + 1) * dmi.IconHeight,
		U1:  float32(x)/float32(dmi.Cols) + uvMargin,
		V1:  float32(y)/float32(dmi.Rows) + uvMargin,
		U2:  float32(x+1)/float32(dmi.Cols) - uvMargin,
		V2:  float32(y+1)/float32(dmi.Rows) - uvMargin,
	}
}
