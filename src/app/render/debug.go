package render

import (
	"math/rand"

	"sdmm/app/render/brush"
	"sdmm/util"
)

var chunkColors map[util.Bounds]util.Color //nolint:unused

// Debug method to render chunks borders.
//
//nolint:unused
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
