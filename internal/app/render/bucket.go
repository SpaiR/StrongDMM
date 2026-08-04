package render

import (
	"sdmm/internal/app/render/brush"
	"sdmm/internal/app/render/bucket/level/chunk/unit"
	"sdmm/internal/util"
)

var (
	MultiZRendering = true

	multiZShadow = util.MakeColor(0, 0, 0, .35)
)

type unitProcessor interface {
	ProcessUnit(unit.Unit) (visible bool)
}

func (r *Render) batchBucketUnits(viewBounds util.Bounds) {
	if MultiZRendering && r.Camera.Level > 1 {
		for level := 1; level < r.Camera.Level; level++ {
			r.batchLevel(level, viewBounds) // Draw everything below.
		}

		// Draw a "shadow" overlay to visually separate different levels.
		brush.RectFilled(viewBounds.X1, viewBounds.Y1, viewBounds.X2, viewBounds.Y2, multiZShadow)
	}

	r.batchLevel(r.Camera.Level, viewBounds) // Draw currently visible level.
}

// batchUnitHighlights batches the active level's selected-unit highlights.
func (r *Render) batchUnitHighlights(viewBounds util.Bounds) {
	if r.overlay == nil {
		return
	}
	visibleLevel := r.bucket.Level(r.Camera.Level)
	if visibleLevel == nil {
		return
	}
	for _, layer := range visibleLevel.Layers {
		for _, chunk := range visibleLevel.ChunksByLayers[layer] {
			if !chunk.ViewBounds.ContainsV(viewBounds) {
				continue
			}
			for _, u := range chunk.UnitsByLayers[layer] {
				if u.ViewBounds().ContainsV(viewBounds) {
					r.batchUnitHighlight(u)
				}
			}
		}
	}
	r.overlay.FlushUnits()
}

func (r *Render) batchLevel(level int, viewBounds util.Bounds) {
	visibleLevel := r.bucket.Level(level)
	if visibleLevel == nil {
		return
	}

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
				effective, underlays, visible := r.appearanceUnit(u)
				if !visible {
					continue
				}
				// Process the effective unit so we get the same one
				if r.unitProcessor != nil && !r.unitProcessor.ProcessUnit(effective) {
					continue
				}
				for _, underlay := range underlays {
					brush.RectTexturedV(underlay.ViewBounds().X1, underlay.ViewBounds().Y1, underlay.ViewBounds().X2, underlay.ViewBounds().Y2, underlay.R(), underlay.G(), underlay.B(), underlay.A(), underlay.Sprite().Texture(), underlay.Sprite().U1, underlay.Sprite().V1, underlay.Sprite().U2, underlay.Sprite().V2)
				}

				brush.RectTexturedV(
					effective.ViewBounds().X1, effective.ViewBounds().Y1, effective.ViewBounds().X2, effective.ViewBounds().Y2,
					effective.R(), effective.G(), effective.B(), effective.A(),
					effective.Sprite().Texture(),
					effective.Sprite().U1, effective.Sprite().V1, effective.Sprite().U2, effective.Sprite().V2,
				)

			}
		}
	}
}

func (r *Render) batchUnitHighlight(u unit.Unit) {
	if r.overlay == nil {
		return
	}
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
