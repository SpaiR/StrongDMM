package dmicon

import (
	"fmt"
	"image"
	"image/draw"
	_ "image/png"
	"os"

	"sdmm/internal/app/window"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/platform"
	"sdmm/internal/third_party/sdmmparser"

	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/rs/zerolog/log"
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
	window.RunLater(func() {
		gl.DeleteTextures(1, &d.Texture)
	})
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
	log.Printf("creating new: [%s]...", path)

	iconMetadata, err := sdmmparser.ParseIconMetadata(path)
	if err != nil {
		log.Printf("unable to parse icon metadata [%s]: %s", path, err)
		return nil, err
	}

	rgba, err := loadRgbaImage(path)
	if err != nil {
		log.Printf("unable to load rgba image [%s]: %s", path, err)
		return nil, err
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

	log.Printf("created: [%s]", path)

	return dmi, nil
}

func loadRgbaImage(path string) (*image.NRGBA, error) {
	f, err := os.Open(path)
	if err != nil {
		log.Printf("unable to open image file [%s]: %s", path, err)
		return nil, err
	}
	defer f.Close()

	imgOs, _, err := image.Decode(f)
	if err != nil {
		log.Printf("unable to decode image file [%s]: %s", path, err)
		return nil, err
	}

	rgba := image.NewNRGBA(imgOs.Bounds())
	draw.Draw(rgba, rgba.Bounds(), imgOs, image.Pt(0, 0), draw.Src)
	if rgba.Stride != rgba.Rect.Size().X*4 {
		return nil, fmt.Errorf("unable to convert image to NRGBA")
	}

	return rgba, nil
}

type State struct {
	Dirs, Frames int
	Sprites      []*Sprite
}

func (s State) Sprite() *Sprite {
	return s.SpriteV(dm.DirDefault)
}

func (s State) SpriteV(dir int) *Sprite {
	return s.SpriteByFrame(dir, 0)
}

func (s State) SpriteByFrame(dir, frame int) *Sprite {
	return s.Sprites[s.dir2idx(dir)+frame%s.Frames*s.Dirs]
}

func (s State) dir2idx(dir int) int {
	if s.Dirs == 1 || dir < dm.DirNorth || dir > dm.DirSouthwest {
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

	if idx+1 <= len(s.Sprites) {
		return idx
	}
	return 0
}

type Sprite struct {
	dmi            *Dmi
	X1, Y1, X2, Y2 int
	U1, V1, U2, V2 float32
}

func (s *Sprite) Dmi() *Dmi {
	return s.dmi
}

func (s Sprite) Image() image.Image {
	return s.dmi.Image
}

func (s Sprite) Texture() uint32 {
	return s.dmi.Texture
}

func (s Sprite) TextureWidth() int {
	return s.dmi.TextureWidth
}

func (s Sprite) TextureHeight() int {
	return s.dmi.TextureHeight
}

func (s Sprite) IconWidth() int {
	return s.dmi.IconWidth
}

func (s Sprite) IconHeight() int {
	return s.dmi.IconHeight
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
