package render

import (
	"math/rand"

	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket"
	"sdmm/dm/dmmap"
	"sdmm/util"
)

type overlayState interface {
	IconSize() int
	HoveredTilePoint() (float32, float32)
	HoverOutOfBounds() bool
}

func Free() {
	bucket.UnitsCache.Free()
}

type Render struct {
	Camera *Camera
	bucket *bucket.Bucket

	overlayState overlayState
}

func New() *Render {
	brush.TryInit()
	return &Render{
		Camera: newCamera(),
		bucket: bucket.New(),
	}
}

func (r *Render) SetOverlayState(state overlayState) {
	r.overlayState = state
}

// UpdateBucket will update the bucket data by the provided level.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm, level int, tilesToUpdate []util.Point) {
	r.bucket.UpdateLevel(dmm, level, tilesToUpdate)
}

// ValidateLevel will ensure that the bucket has data by the provided level.
func (r *Render) ValidateLevel(dmm *dmmap.Dmm, level int) {
	if r.bucket.Level(level) == nil {
		r.UpdateBucket(dmm, level, nil)
	}
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
	r.batchOverlay()
	brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
}

func (r *Render) batchBucketUnits(width, height float32) {
	x1, y1, x2, y2 := r.viewportBounds(width, height)

	// Iterate through every layer to render.
	for _, layer := range r.visibleLevel().Layers {
		// Iterate though chunks with units on the rendered layer.
		for _, chunk := range r.visibleLevel().ChunksByLayers[layer] {
			// Out of bounds = skip.
			if !chunk.ViewBounds.ContainsV(x1, y1, x2, y2) {
				continue
			}

			// Get all units in the chunk for the specific layer.
			for _, u := range chunk.UnitsByLayers[layer] {
				// Out of bounds = skip.
				if !u.ViewBounds.ContainsV(x1, y1, x2, y2) {
					continue
				}

				brush.RectTexturedV(
					u.ViewBounds.X1, u.ViewBounds.Y1, u.ViewBounds.X2, u.ViewBounds.Y2,
					u.R, u.G, u.B, u.A,
					u.Sp.Texture(),
					u.Sp.U1, u.Sp.V1, u.Sp.U2, u.Sp.V2,
				)
			}
		}
	}
}

func (r *Render) viewportBounds(width, height float32) (x1, y1, x2, y2 float32) {
	// Get transformed bounds of the map, so we can ignore out of bounds units.
	w := width / r.Camera.Scale
	h := height / r.Camera.Scale

	x1 = -r.Camera.ShiftX
	y1 = -r.Camera.ShiftY
	x2 = x1 + w
	y2 = y1 + h

	return x1, y1, x2, y2
}

var chunkColors map[bucket.Bounds]brush.Color

// Debug method to render chunks borders.
func (r *Render) batchChunksVisuals() {
	if chunkColors == nil {
		println("[debug] CHUNKS VISUALISATION ENABLED!")
		chunkColors = make(map[bucket.Bounds]brush.Color)
	}

	for _, c := range r.visibleLevel().Chunks {
		var chunkColor brush.Color
		if color, ok := chunkColors[c.MapBounds]; ok {
			chunkColor = color
		} else {
			chunkColor = brush.Color{R: rand.Float32(), G: rand.Float32(), B: rand.Float32(), A: .25}
			chunkColors[c.MapBounds] = chunkColor
		}

		brush.RectFilled(c.ViewBounds.X1, c.ViewBounds.Y1, c.ViewBounds.X2, c.ViewBounds.Y2, chunkColor)
		brush.RectV(c.ViewBounds.X1, c.ViewBounds.Y1, c.ViewBounds.X2, c.ViewBounds.Y2, 1, 1, 1, .5)
	}
}

// Returns current visible level to render.
func (r *Render) visibleLevel() *bucket.Level {
	return r.bucket.Level(r.Camera.Level)
}

var (
	activeTileCol       = brush.Color{R: 1, G: 1, B: 1, A: 0.25}
	activeTileBorderCol = brush.Color{R: 1, G: 1, B: 1, A: 1}
)

// Draws the map overlays, like: hovered tile borders, areas borders etc.
func (r *Render) batchOverlay() {
	if !r.overlayState.HoverOutOfBounds() {
		size := float32(r.overlayState.IconSize())

		x1, y1 := r.overlayState.HoveredTilePoint()
		x2, y2 := x1+size, y1+size

		brush.RectFilled(x1, y1, x2, y2, activeTileCol)
		brush.Rect(x1, y1, x2, y2, activeTileBorderCol)
	}
}

// Clean OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
