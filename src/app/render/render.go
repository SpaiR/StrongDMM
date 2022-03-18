package render

import (
	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket"
	"sdmm/dmapi/dmmap"
	"sdmm/util"
)

type Render struct {
	Camera *Camera

	bucket *bucket.Bucket

	overlay       overlay
	unitProcessor unitProcessor
}

func New() *Render {
	brush.TryInit()
	return &Render{
		Camera: newCamera(),
		bucket: bucket.New(),
	}
}

func (r *Render) SetUnitProcessor(processor unitProcessor) {
	r.unitProcessor = processor
}

func (r *Render) SetOverlay(state overlay) {
	r.overlay = state
}

func (r *Render) SetActiveLevel(dmm *dmmap.Dmm, activeLevel int) {
	r.Camera.Level = activeLevel
	if r.bucket.Level(activeLevel) == nil { // Ensure level exists
		r.UpdateBucket(dmm, activeLevel)
	}
}

// UpdateBucketV will update the bucket data by the provided level.
func (r *Render) UpdateBucketV(dmm *dmmap.Dmm, level int, tilesToUpdate []util.Point) {
	r.bucket.UpdateLevel(dmm, level, tilesToUpdate)
}

// UpdateBucket will ensure that the bucket has data by the provided level.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm, level int) {
	r.UpdateBucketV(dmm, level, nil)
}

func (r *Render) Draw(width, height float32) {
	r.prepare()
	r.draw(width, height)
	r.cleanup()
}

// Initialize OpenGL state.
func (r *Render) prepare() {
	gl.Enable(gl.BLEND)
	gl.BlendFunc(gl.SRC_ALPHA, gl.ONE_MINUS_SRC_ALPHA)
	gl.BlendEquation(gl.FUNC_ADD)
	gl.ActiveTexture(gl.TEXTURE0)
}

func (r *Render) draw(width, height float32) {
	r.batchBucketUnits(r.viewportBounds(width, height))
	//r.batchChunksVisuals()
	r.batchOverlayAreasBorders()
	r.batchOverlayAreas()
	brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
}

// Clean OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}

func (r *Render) viewportBounds(width, height float32) util.Bounds {
	// Get transformed bounds of the map, so we can ignore out of bounds units.
	w := width / r.Camera.Scale
	h := height / r.Camera.Scale

	x1 := -r.Camera.ShiftX
	y1 := -r.Camera.ShiftY
	x2 := x1 + w
	y2 := y1 + h

	return util.Bounds{
		X1: x1,
		Y1: y1,
		X2: x2,
		Y2: y2,
	}
}
