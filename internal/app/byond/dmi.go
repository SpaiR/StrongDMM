package byond

import (
	"fmt"
	"image"
	"image/draw"
	"image/png"
	"os"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/third_party/sdmmparser"
)

type Dmi struct {
	IconWidth, IconHeight       int
	TextureWidth, TextureHeight int
	Cols, Rows                  int
	Texture                     uint32
	States                      map[string]*DmiState
}

func (d *Dmi) free() {
	gl.DeleteTextures(1, &d.Texture)
}

func (d *Dmi) State(state string) (*DmiState, error) {
	if dmiState, ok := d.States[state]; ok {
		return dmiState, nil
	}
	if dmiState, ok := d.States[""]; ok {
		return dmiState, nil
	}
	return nil, fmt.Errorf("no dmi state by name [%s]", state)
}

func newDmi(path string) (*Dmi, error) {
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
		States:        make(map[string]*DmiState),
	}

	spriteIdx := 0

	for _, state := range iconMetadata.States {
		dmiState := &DmiState{
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
	imgFile, err := os.Open(path)
	if err != nil {
		return nil
	}
	defer imgFile.Close()

	img, err := png.Decode(imgFile)
	if err != nil {
		return nil
	}

	switch trueImg := img.(type) {
	case *image.RGBA:
		return trueImg
	default:
		rgba := image.NewRGBA(trueImg.Bounds())
		draw.Draw(rgba, trueImg.Bounds(), trueImg, image.Pt(0, 0), draw.Src)
		return rgba
	}
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

type DmiState struct {
	Dirs, Frames int
	Sprites      []*DmiSprite
}

func (d *DmiState) Sprite() *DmiSprite {
	return d.SpriteD(DirDefault)
}

func (d *DmiState) SpriteD(dir int) *DmiSprite {
	return d.SpriteDF(dir, 0)
}

func (d *DmiState) SpriteDF(dir, frame int) *DmiSprite {
	return d.Sprites[d.dir2idx(dir)+frame%d.Frames*d.Dirs]
}

func (d *DmiState) dir2idx(dir int) int {
	if d.Dirs == 1 || dir < DirNorth || dir > DirSouthwest {
		return 0
	}

	idx := 0
	switch dir {
	case DirSouth:
		idx = 0
	case DirNorth:
		idx = 1
	case DirEast:
		idx = 2
	case DirWest:
		idx = 3
	case DirSoutheast:
		idx = 4
	case DirSouthwest:
		idx = 5
	case DirNortheast:
		idx = 6
	case DirNorthwest:
		idx = 7
	}

	if idx+1 <= len(d.Sprites) {
		return idx
	}
	return 0
}

type DmiSprite struct {
	dmi            *Dmi
	X1, Y1, X2, Y2 int
	U1, V1, U2, V2 float32
}

func (d DmiSprite) Texture() uint32 {
	return d.dmi.Texture
}

func (d DmiSprite) TextureWidth() int {
	return d.dmi.TextureWidth
}

func (d DmiSprite) TextureHeight() int {
	return d.dmi.TextureHeight
}

func (d DmiSprite) IconWidth() int {
	return d.dmi.IconWidth
}

func (d DmiSprite) IconHeight() int {
	return d.dmi.IconHeight
}

func newDmiSprite(dmi *Dmi, idx int) *DmiSprite {
	const uvMargin = .000001
	x := idx % dmi.Cols
	y := idx / dmi.Cols
	return &DmiSprite{
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
