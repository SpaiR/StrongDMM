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

type overlayState interface {
	HoverOutOfBounds() bool
	HoveredTileBounds() util.Bounds
	ModifiedTiles() []util.Bounds
}

type unitProcessor interface {
	ProcessUnit(unit.Unit) (visible bool)
}

type Render struct {
	Camera *Camera

	bucket *bucket.Bucket

	overlayState  overlayState
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
	visibleLevel := r.bucket.Level(r.Camera.Level)

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
	w := width / r.Camera.Scale
	h := height / r.Camera.Scale

	x1 = -r.Camera.ShiftX
	y1 = -r.Camera.ShiftY
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

	visibleLevel := r.bucket.Level(r.Camera.Level)

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

var (
	activeTileCol       = util.MakeColor(1, 1, 1, 0.25)
	activeTileBorderCol = util.MakeColor(1, 1, 1, 1)
	modifiedTileCol     = util.MakeColor(0, 1, 0, 1)
)

// Draws the map overlays, like: hovered tile borders, areas borders etc.
func (r *Render) batchOverlay() {
	// Hovered tile
	if !r.overlayState.HoverOutOfBounds() {
		t := r.overlayState.HoveredTileBounds()
		brush.RectFilled(t.X1, t.Y1, t.X2, t.Y2, activeTileCol)
		brush.Rect(t.X1, t.Y1, t.X2, t.Y2, activeTileBorderCol)
	}

	// Modified tiles
	for _, c := range r.overlayState.ModifiedTiles() {
		brush.Rect(c.X1, c.Y1, c.X2, c.Y2, modifiedTileCol)
	}
}

// Clean OpenGL state after rendering.
func (r *Render) cleanup() {
	gl.Disable(gl.BLEND)
}
