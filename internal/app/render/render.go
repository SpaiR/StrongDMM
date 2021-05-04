package render

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/render/brush"
	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

type overlayState interface {
	IconSize() int
	HoveredTilePoint() (x, y float32)
	HoverOutOfBounds() bool
}

type Render struct {
	Camera *Camera
	bucket *bucket

	overlayState overlayState
}

func New() *Render {
	brush.TryInit()
	return &Render{
		Camera: &Camera{Scale: 1},
		bucket: &bucket{},
	}
}

func (r *Render) SetOverlayState(state overlayState) {
	r.overlayState = state
}

// UpdateBucket used to update internal data about the map.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm) {
	log.Printf("[render] updating bucket with [%s]...", dmm.Path.Readable)
	r.bucket.update(dmm)
	log.Println("[render] bucket updated")
}

func (r *Render) Draw(width, height float32) {
	r.prepare()
	r.draw(width, height)
	r.cleanup()
}

// prepare method to initialize OpenGL state.
func (r *Render) prepare() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.ActiveTexture(gl.TEXTURE0)
}

func (r *Render) draw(width, height float32) {
	r.batchBucketUnits(width, height)
	r.batchOverlay()
	brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
}

func (r *Render) batchBucketUnits(width, height float32) {
	// Get transformed bounds of the map, so we can ignore out of bounds units.
	w := width / r.Camera.Scale
	h := height / r.Camera.Scale
	x1 := -r.Camera.ShiftX
	y1 := -r.Camera.ShiftY
	x2 := x1 + w
	y2 := y1 + h

	// Batch all bucket units.
	for _, u := range r.bucket.units {
		if u.isInBounds(x1, y1, x2, y2) {
			brush.RectTextured(u.x1, u.y1, u.x2, u.y2, u.r, u.g, u.b, u.a, u.sp.Texture(), u.sp.U1, u.sp.V1, u.sp.U2, u.sp.V2)
		}
	}
}

type color struct {
	r, g, b, a float32
}

var (
	activeTileCol       = color{r: 1, g: 1, b: 1, a: 0.25}
	activeTileBorderCol = color{r: 1, g: 1, b: 1, a: 1}
)

func (r *Render) batchOverlay() {
	if !r.overlayState.HoverOutOfBounds() {
		size := float32(r.overlayState.IconSize())

		x1, y1 := r.overlayState.HoveredTilePoint()
		x2, y2 := x1+size, y1+size

		brush.RectFilled(x1, y1, x2, y2, activeTileCol.r, activeTileCol.g, activeTileCol.b, activeTileCol.a)
		brush.Rect(x1, y1, x2, y2, activeTileBorderCol.r, activeTileBorderCol.g, activeTileBorderCol.b, activeTileBorderCol.a)
	}
}

// cleanup method to cleanup OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
