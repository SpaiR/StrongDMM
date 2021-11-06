package render

import (
	"math/rand"

	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket"
	"sdmm/app/render/bucket/level/chunk/unit"
	"sdmm/dmapi/dmmap"
	"sdmm/util"
)

type OverlayArea interface {
	Bounds() util.Bounds
	FillColor() util.Color
	BorderColor() util.Color
}

type overlay interface {
	Areas() []OverlayArea
	Flush()
}

type unitProcessor interface {
	ProcessUnit(unit.Unit) (visible bool)
}

type Render struct {
	camera *Camera

	bucket *bucket.Bucket

	overlay       overlay
	unitProcessor unitProcessor
}

func (r *Render) Camera() *Camera {
	return r.camera
}

func New() *Render {
	brush.TryInit()
	return &Render{
		camera: newCamera(),
		bucket: bucket.New(),
	}
}

func (r *Render) SetUnitProcessor(processor unitProcessor) {
	r.unitProcessor = processor
}

func (r *Render) SetOverlay(state overlay) {
	r.overlay = state
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
	r.batchBucketUnits(width, height)
	//r.batchChunksVisuals()
	r.batchOverlayTiles()
	brush.Draw(width, height, r.camera.ShiftX, r.camera.ShiftY, r.camera.Scale)
}

func (r *Render) batchBucketUnits(width, height float32) {
	x1, y1, x2, y2 := r.viewportBounds(width, height)
	visibleLevel := r.bucket.Level(r.camera.Level)

	// Iterate through every layer to render.
	for _, layer := range visibleLevel.Layers {
		// Iterate through chunks with units on the rendered layer.
		for _, chunk := range visibleLevel.ChunksByLayers[layer] {
			// Out of bounds = skip.
			if !chunk.ViewBounds.ContainsV(x1, y1, x2, y2) {
				continue
			}

			// Get all units in the chunk for the specific layer.
			for _, u := range chunk.UnitsByLayers[layer] {
				// Out of bounds = skip
				if !u.ViewBounds().ContainsV(x1, y1, x2, y2) {
					continue
				}
				// Process unit
				if !r.unitProcessor.ProcessUnit(u) {
					continue
				}

				brush.RectTexturedV(
					u.ViewBounds().X1, u.ViewBounds().Y1, u.ViewBounds().X2, u.ViewBounds().Y2,
					u.R(), u.G(), u.B(), u.A(),
					u.Sprite().Texture(),
					u.Sprite().U1, u.Sprite().V1, u.Sprite().U2, u.Sprite().V2,
				)
			}
		}
	}
}

func (r *Render) viewportBounds(width, height float32) (x1, y1, x2, y2 float32) {
	// Get transformed bounds of the map, so we can ignore out of bounds units.
	w := width / r.camera.Scale
	h := height / r.camera.Scale

	x1 = -r.camera.ShiftX
	y1 = -r.camera.ShiftY
	x2 = x1 + w
	y2 = y1 + h

	return x1, y1, x2, y2
}

var chunkColors map[util.Bounds]util.Color

// Debug method to render chunks borders.
func (r *Render) batchChunksVisuals() {
	if chunkColors == nil {
		println("[debug] CHUNKS VISUALISATION ENABLED!")
		chunkColors = make(map[util.Bounds]util.Color)
	}

	visibleLevel := r.bucket.Level(r.camera.Level)

	for _, c := range visibleLevel.Chunks {
		var chunkColor util.Color
		if color, ok := chunkColors[c.MapBounds]; ok {
			chunkColor = color
		} else {
			chunkColor = util.MakeColor(rand.Float32(), rand.Float32(), rand.Float32(), .25)
			chunkColors[c.MapBounds] = chunkColor
		}

		brush.RectFilled(c.ViewBounds.X1, c.ViewBounds.Y1, c.ViewBounds.X2, c.ViewBounds.Y2, chunkColor)
		brush.RectV(c.ViewBounds.X1, c.ViewBounds.Y1, c.ViewBounds.X2, c.ViewBounds.Y2, 1, 1, 1, .5)
	}
}

// Draw an overlay for the map tiles.
func (r *Render) batchOverlayTiles() {
	if r.overlay == nil {
		return
	}

	for _, t := range r.overlay.Areas() {
		brush.RectFilled(t.Bounds().X1, t.Bounds().Y1, t.Bounds().X2, t.Bounds().Y2, t.FillColor())
		brush.Rect(t.Bounds().X1, t.Bounds().Y1, t.Bounds().X2, t.Bounds().Y2, t.BorderColor())
	}

	r.overlay.Flush()
}

// Clean OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
