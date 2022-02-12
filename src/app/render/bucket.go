package render

import (
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket/level/chunk/unit"
	"sdmm/util"
)

var (
	MultiZRendering = true

	multiZShadow = util.MakeColor(0, 0, 0, .35)
)

type unitProcessor interface {
	ProcessUnit(unit.Unit) (visible bool)
}

func (r *Render) batchBucketUnits(viewBounds util.Bounds) {
	if MultiZRendering && r.camera.Level > 1 {
		for level := 1; level < r.camera.Level; level++ {
			r.batchLevel(level, viewBounds, false) // Draw everything below.
		}

		// Draw a "shadow" overlay to visually separate different levels.
		brush.RectFilled(viewBounds.X1, viewBounds.Y1, viewBounds.X2, viewBounds.Y2, multiZShadow)
	}

	r.batchLevel(r.camera.Level, viewBounds, true) // Draw currently visible level.
	r.overlay.FlushUnits()
}

func (r *Render) batchLevel(level int, viewBounds util.Bounds, withUnitHighlight bool) {
	visibleLevel := r.bucket.Level(level)

	// Iterate through every layer to render.
	for _, layer := range visibleLevel.Layers {
		// Iterate through chunks with units on the rendered layer.
		for _, chunk := range visibleLevel.ChunksByLayers[layer] {
			// Out of bounds = skip.
			if !chunk.ViewBounds.ContainsV(viewBounds) {
				continue
			}

			// Get all units in the chunk for the specific layer.
			for _, u := range chunk.UnitsByLayers[layer] {
				// Out of bounds = skip
				if !u.ViewBounds().ContainsV(viewBounds) {
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

				if withUnitHighlight {
					r.batchUnitHighlight(u)
				}
			}
		}
	}
}

func (r *Render) batchUnitHighlight(u unit.Unit) {
	if highlight := r.overlay.Units()[u.Instance().Id()]; highlight != nil {
		r, g, b, a := highlight.Color().RGBA()
		brush.RectTexturedV(
			u.ViewBounds().X1, u.ViewBounds().Y1, u.ViewBounds().X2, u.ViewBounds().Y2,
			r, g, b, a,
			u.Sprite().Texture(),
			u.Sprite().U1, u.Sprite().V1, u.Sprite().U2, u.Sprite().V2,
		)
	}
}
