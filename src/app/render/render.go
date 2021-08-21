package render

import (
	"log"
	"math/rand"

	"github.com/go-gl/gl/v3.3-core/gl"
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket"
	"sdmm/dm/dmmap"
	"sdmm/util"
)

type overlayState interface {
	IconSize() int
	HoveredTilePoint() (x, y float32)
	HoverOutOfBounds() bool
}

func Free() {
	bucket.UnitsCache.Free()
}

type Render struct {
	Camera *Camera
	bucket *bucket.Bucket

	overlayState overlayState

	tmpDmmToUpdateBucket   *dmmap.Dmm
	tmpTilesToUpdateBucket []util.Point
	tmpLevelToUpdateBucket int
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

// UpdateBucket used to update internal data about the map.
func (r *Render) UpdateBucket(dmm *dmmap.Dmm, level int) {
	r.UpdateBucketV(dmm, level, nil)
}

func (r *Render) UpdateBucketV(dmm *dmmap.Dmm, level int, tilesToUpdate []util.Point) {
	r.tmpDmmToUpdateBucket = dmm
	r.tmpTilesToUpdateBucket = append(r.tmpTilesToUpdateBucket, tilesToUpdate...)
	r.tmpLevelToUpdateBucket = level
}

func (r *Render) updateBucketState() {
	if r.tmpDmmToUpdateBucket != nil && !r.bucket.Updating {
		log.Printf("[render] updating bucket with [%s]...", r.tmpDmmToUpdateBucket.Path.Readable)
		r.bucket.UpdateV(r.tmpDmmToUpdateBucket, r.tmpLevelToUpdateBucket, r.tmpTilesToUpdateBucket)
		r.tmpDmmToUpdateBucket = nil
		r.tmpTilesToUpdateBucket = nil
		r.tmpLevelToUpdateBucket = 1 // Reset to 1 just in case.
		log.Println("[render] bucket updated")
	}
}

func (r *Render) Draw(width, height float32) {
	r.updateBucketState()
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
	//r.batchChunksVisuals()
	r.batchOverlay()
	brush.Draw(width, height, r.Camera.ShiftX, r.Camera.ShiftY, r.Camera.Scale)
}

func (r *Render) batchBucketUnits(width, height float32) {
	// Get transformed bounds of the map, so we can ignore out of bounds units.
	w := width / r.Camera.Scale
	h := height / r.Camera.Scale

	// Get bounds of the current viewport.
	x1 := -r.Camera.ShiftX
	y1 := -r.Camera.ShiftY
	x2 := x1 + w
	y2 := y1 + h

	// Iterate though every available layer which can be rendered.
	for _, layer := range r.bucket.Layers {
		// Get a chunk, which has units with currently rendered layer.
		for _, chunk := range r.bucket.ChunksByLayers[layer] {
			// Skip out of view chunks
			if !chunk.ViewBounds.ContainsV(x1, y1, x2, y2) {
				continue
			}

			// Get all units in the chunk for the specific layer.
			for _, u := range chunk.UnitsByLayers[layer] {
				// Skip out of view units
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

var chunkColors map[bucket.Bounds]brush.Color

func (r *Render) batchChunksVisuals() {
	if chunkColors == nil {
		println("[debug] CHUNKS VISUALISATION ENABLED!")
		chunkColors = make(map[bucket.Bounds]brush.Color)
	}

	for _, c := range r.bucket.Chunks {
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

var (
	activeTileCol       = brush.Color{R: 1, G: 1, B: 1, A: 0.25}
	activeTileBorderCol = brush.Color{R: 1, G: 1, B: 1, A: 1}
)

func (r *Render) batchOverlay() {
	if !r.overlayState.HoverOutOfBounds() {
		size := float32(r.overlayState.IconSize())

		x1, y1 := r.overlayState.HoveredTilePoint()
		x2, y2 := x1+size, y1+size

		brush.RectFilled(x1, y1, x2, y2, activeTileCol)
		brush.Rect(x1, y1, x2, y2, activeTileBorderCol)
	}
}

// cleanup method to clean up OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
