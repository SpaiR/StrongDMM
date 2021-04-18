package render

import (
	"log"

	"github.com/go-gl/gl/v3.3-core/gl"

	"github.com/SpaiR/strongdmm/internal/app/render/program"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

type Render struct {
	Camera *Camera
	bucket *bucket

	dmmProgram *program.Dmm
}

func New() *Render {
	return &Render{
		Camera:     &Camera{Scale: 1},
		bucket:     &bucket{},
		dmmProgram: program.NewDmm(),
	}
}

// UpdateBucket used to update internal data about the map.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm) {
	log.Printf("[render] updating bucket with [%s]...", dmm.Path.Readable)
	r.bucket.update(dmm)
	r.dmmProgram.SetData(r.bucket.data)
	log.Println("[render] bucket updated")
}

func (r *Render) Dispose() {
	log.Println("[render] disposing dmm program...")
	r.dmmProgram.Dispose()
	log.Println("[render] dmm program disposed")
}

func (r *Render) Draw(width, height float32) {
	r.prepare()
	r.drawDmm(width, height)
	r.cleanup()
}

// prepare method to initialize OpenGL state.
func (r *Render) prepare() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.ActiveTexture(gl.TEXTURE0)
}

func (r *Render) drawDmm(width, height float32) {
	r.dmmProgram.UpdateTransform(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)

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
			r.dmmProgram.BatchRect(unit.idx)
		}
	}

	// Draw all batched units.
	r.dmmProgram.BatchFlush()
}

// cleanup method to cleanup OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
