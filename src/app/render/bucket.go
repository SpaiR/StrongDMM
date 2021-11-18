package render

import (
	"sdmm/app/render/brush"
	"sdmm/app/render/bucket/level/chunk/unit"
	"sdmm/util"
)

type unitProcessor interface {
	ProcessUnit(unit.Unit) (visible bool)
}

func (r *Render) batchBucketUnits(viewBounds util.Bounds) {
	visibleLevel := r.bucket.Level(r.camera.Level)

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

				r, g, b, a := r.unitColor(u)

				brush.RectTexturedV(
					u.ViewBounds().X1, u.ViewBounds().Y1, u.ViewBounds().X2, u.ViewBounds().Y2,
					r, g, b, a,
					u.Sprite().Texture(),
					u.Sprite().U1, u.Sprite().V1, u.Sprite().U2, u.Sprite().V2,
				)
			}
		}
	}

	r.overlay.FlushUnits()
}

func (r *Render) unitColor(u unit.Unit) (float32, float32, float32, float32) {
	if highlight := r.overlay.Units()[u.Instance().Id()]; highlight != nil {
		return highlight.Color().RGBA()
	}
	return u.R(), u.G(), u.B(), u.A()
}
