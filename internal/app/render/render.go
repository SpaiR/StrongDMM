package render

import (
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/app/render/program"
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

	dmmProgram   *program.Dmm
	overlayState overlayState
}

func New() *Render {
	return &Render{
		Camera:     &Camera{Scale: 1},
		bucket:     &bucket{},
		dmmProgram: program.NewDmm(),
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

func (r *Render) Dispose() {
	log.Println("[render] disposing dmm program...")
	r.dmmProgram.Dispose()
	log.Println("[render] dmm program disposed")
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
	r.dmmProgram.SetData(r.bucket.data)
	r.dmmProgram.UpdateTransform(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)

	r.batchBucketUnits(width, height)
	r.batchOverlay()

	// Draw all batched units.
	r.dmmProgram.BatchFlush()
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
	for _, unit := range r.bucket.units {
		if unit.isInBounds(x1, y1, x2, y2) {
			r.dmmProgram.BatchTexture(unit.sp.Texture())
			r.dmmProgram.BatchRectIdx(unit.idx)
		}
	}
}

func (r *Render) batchOverlay() {
	if !r.overlayState.HoverOutOfBounds() {
		x, y := r.overlayState.HoveredTilePoint()
		s := float32(r.overlayState.IconSize())
		r.dmmProgram.BatchTexture(program.OverlayTexture())
		r.dmmProgram.BatchRect(x, y, s, 1, 1, 1, .45, 0, 0, 1, 1)
	}
}

// cleanup method to cleanup OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
