package dmicon

import (
	"fmt"
	"image"
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/app/byond"
	"github.com/SpaiR/strongdmm/third_party/sdmmparser"
	"github.com/SpaiR/strongdmm/third_party/stbi"
)

type Dmi struct {
	IconWidth, IconHeight       int
	TextureWidth, TextureHeight int
	Cols, Rows                  int
	Texture                     uint32
	States                      map[string]*State
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
		return nil, err
	}

	img := loadImage(path)
	width := img.Bounds().Dx()
	height := img.Bounds().Dy()

	dmi := &Dmi{
		IconWidth:     iconMetadata.Width,
		IconHeight:    iconMetadata.Height,
		TextureWidth:  width,
		TextureHeight: height,
		Cols:          width / iconMetadata.Width,
		Rows:          height / iconMetadata.Height,
		Texture:       createTexture(img),
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

func loadImage(path string) *image.RGBA {
	img, err := stbi.Load(path)
	if err != nil {
		log.Println("unable to read image by path: ", path, err)
	}
	return img
}

func createTexture(img *image.RGBA) uint32 {
	var lastTexture int32
	var handle uint32

	gl.GetIntegerv(gl.TEXTURE_BINDING_2D, &lastTexture)
	gl.GenTextures(1, &handle)
	gl.BindTexture(gl.TEXTURE_2D, handle)

	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_S, gl.CLAMP_TO_EDGE)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_WRAP_T, gl.CLAMP_TO_EDGE)

	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MIN_FILTER, gl.NEAREST_MIPMAP_LINEAR)
	gl.TexParameteri(gl.TEXTURE_2D, gl.TEXTURE_MAG_FILTER, gl.NEAREST)

	gl.TexImage2D(gl.TEXTURE_2D, 0, gl.RGBA, int32(img.Bounds().Dx()), int32(img.Bounds().Dy()), 0, gl.RGBA, gl.UNSIGNED_BYTE, gl.Ptr(img.Pix))
	gl.GenerateMipmap(gl.TEXTURE_2D)

	gl.BindTexture(gl.TEXTURE_2D, uint32(lastTexture))

	return handle
}

type State struct {
	Dirs, Frames int
	Sprites      []*Sprite
}

func (d *State) Sprite() *Sprite {
	return d.SpriteD(byond.DirDefault)
}

func (d *State) SpriteD(dir int) *Sprite {
	return d.SpriteDF(dir, 0)
}

func (d *State) SpriteDF(dir, frame int) *Sprite {
	return d.Sprites[d.dir2idx(dir)+frame%d.Frames*d.Dirs]
}

func (d *State) dir2idx(dir int) int {
	if d.Dirs == 1 || dir < byond.DirNorth || dir > byond.DirSouthwest {
		return 0
	}

	idx := 0
	switch dir {
	case byond.DirSouth:
		idx = 0
	case byond.DirNorth:
		idx = 1
	case byond.DirEast:
		idx = 2
	case byond.DirWest:
		idx = 3
	case byond.DirSoutheast:
		idx = 4
	case byond.DirSouthwest:
		idx = 5
	case byond.DirNortheast:
		idx = 6
	case byond.DirNorthwest:
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
