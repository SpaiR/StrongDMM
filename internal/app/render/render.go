package render

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/render/program"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

type Render struct {
	State  *State
	bucket *bucket

	dmmProgram *program.Dmm
}

func New() *Render {
	return &Render{
		State:      newState(),
		bucket:     &bucket{},
		dmmProgram: program.NewDmm(),
	}
}

// UpdateBucket used to update internal data about the map.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm) {
	log.Printf("[render] updating bucket with [%s]...", dmm.Path.Readable)
	r.bucket.update(dmm)
	r.dmmProgram.UpdateData(r.bucket.data)
	log.Println("[render] bucket updated")
}

func (r *Render) Dispose() {
	log.Println("[render] disposing dmm program...")
	r.dmmProgram.Dispose()
	log.Println("[render] dmm program disposed")
}

func (r *Render) Draw(width, height float32) {
	r.prepare()

	// We will share the same transformation matrix for all renders.
	mtxTransform := r.createTransformMatrix(width, height)
	r.drawDmm(width, height, mtxTransform)

	r.cleanup()
}

func (r *Render) drawDmm(width, height float32, mtxTransform mgl32.Mat4) {
	r.dmmProgram.UpdateTransform(mtxTransform)

	// Convert our width/height to scaled values.
	width = width / r.State.Scale
	height = height / r.State.Scale

	// Draw all bucket units
	for _, unit := range r.bucket.units {
		// Ignore out of bounds units.
		if r.isUnitInBounds(unit, width, height) {
			r.dmmProgram.BatchPush(unit.dataIndex(), unit.sp.Texture())
		}
	}

	r.dmmProgram.BatchFlush()
}

// prepare method to initialize OpenGL state.
func (r *Render) prepare() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.ActiveTexture(gl.TEXTURE0)
}

// createTransformMatrix will create a transformation matrix to apply it during the map rendering.
func (r *Render) createTransformMatrix(width, height float32) mgl32.Mat4 {
	view := mgl32.Ortho(0, width, 0, height, -1, 1).Mul4(mgl32.Scale2D(r.State.Scale, r.State.Scale).Mat4())
	model := mgl32.Ident4().Mul4(mgl32.Translate2D(r.State.ShiftX, r.State.ShiftY).Mat4())
	return view.Mul4(model)
}

func (r *Render) isUnitInBounds(u unit, w, h float32) bool {
	bx1, by1, bx2, by2 := u.x1+r.State.ShiftX, u.y1+r.State.ShiftY, u.x2+r.State.ShiftX, u.y2+r.State.ShiftY
	return bx1 >= 0 || by1 >= 0 || bx2 <= w || by2 <= h
}

// cleanup method to cleanup OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
