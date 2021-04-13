package render

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/render/program"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/mathgl/mgl32"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

var (
	indicesCache []uint32 // Reuse all the same buffer to avoid allocations.
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

func (r *Render) UpdateBucket(dmm *dmmap.Dmm) {
	log.Printf("[render] updating bucket with [%s]...", dmm.Path.Readable)
	r.bucket.update(dmm)
	r.dmmProgram.SetArrayBuffer(r.bucket.data)
	log.Println("[render] bucket updated")
}

func (r *Render) Dispose() {
	log.Println("[render] disposing dmm program...")
	r.dmmProgram.Dispose()
	log.Println("[render] dmm program disposed")
}

func (r *Render) Draw(width, height float32) {
	// Initialize OpenGL state.
	r.prepare()

	mtxTransform := r.createTransformMatrix(width, height)
	r.drawDmm(width, height, mtxTransform)

	// Major cleanup for OpenGL state.
	r.cleanup()
}

func (r *Render) drawDmm(width, height float32, mtxTransform mgl32.Mat4) {
	// Ensure that indices cache is empty.
	indicesCache = indicesCache[:0]

	r.dmmProgram.Prepare(mtxTransform)

	// Here we will place our active texture.
	var activeTexture uint32

	// Convert our width/height to scaled values.
	width = width / r.State.Scale
	height = height / r.State.Scale

	// Draw all bucket units
	for _, unit := range r.bucket.units {
		// Ignore out of bounds units.
		if r.isUnitOutOfBounds(unit, width, height) {
			continue
		}

		texture := unit.sp.Texture()

		// Sort of texture batching.
		// More effectively would be to merge all textures into one atlas and do not switch texture at all.
		if texture != activeTexture {
			if activeTexture != 0 && len(indicesCache) > 0 {
				r.flushDmmIndices()
			}

			gl.BindTexture(gl.TEXTURE_2D, texture)
			activeTexture = texture
		}

		// Push data into the same indices slice to avoid unnecessary allocations.
		unit.pushIndices(&indicesCache)
	}

	// If we have something to draw - draw it.
	if activeTexture != 0 && len(indicesCache) > 0 {
		r.flushDmmIndices()
	}

	r.dmmProgram.Cleanup()
}

func (r *Render) flushDmmIndices() {
	r.dmmProgram.UpdateElementArrayBuffer(indicesCache)
	gl.DrawElements(gl.TRIANGLES, int32(len(indicesCache)), gl.UNSIGNED_INT, gl.PtrOffset(0))
	indicesCache = indicesCache[:0]
}

func (r *Render) prepare() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.ActiveTexture(gl.TEXTURE0)
}

func (r *Render) createTransformMatrix(width, height float32) mgl32.Mat4 {
	view := mgl32.Ortho(0, width, 0, height, -1, 1).Mul4(mgl32.Scale2D(r.State.Scale, r.State.Scale).Mat4())
	model := mgl32.Ident4().Mul4(mgl32.Translate2D(r.State.ShiftX, r.State.ShiftY).Mat4())
	return view.Mul4(model)
}

func (r *Render) isUnitOutOfBounds(u unit, w, h float32) bool {
	bx1, by1, bx2, by2 := u.x1+r.State.ShiftX, u.y1+r.State.ShiftY, u.x2+r.State.ShiftX, u.y2+r.State.ShiftY
	return bx1 > w || by1 > h || bx2 < 0 || by2 < 0
}

func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
