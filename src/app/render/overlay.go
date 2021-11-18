package render

import (
	"sdmm/app/render/brush"
	"sdmm/util"
)

type OverlayArea interface {
	Bounds() util.Bounds
	FillColor() util.Color
	BorderColor() util.Color
}

type HighlightUnit interface {
	Id() uint64
	Color() util.Color
}

type overlay interface {
	Areas() []OverlayArea
	FlushAreas()
	Units() map[uint64]HighlightUnit
	FlushUnits()
}

// Draw an overlay for the map tiles.
func (r *Render) batchOverlayAreas() {
	if r.overlay == nil {
		return
	}

	for _, t := range r.overlay.Areas() {
		brush.RectFilled(t.Bounds().X1, t.Bounds().Y1, t.Bounds().X2, t.Bounds().Y2, t.FillColor())
		brush.Rect(t.Bounds().X1, t.Bounds().Y1, t.Bounds().X2, t.Bounds().Y2, t.BorderColor())
	}

	r.overlay.FlushAreas()
}
