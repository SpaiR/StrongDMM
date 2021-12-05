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

	for _, a := range r.overlay.Areas() {
		brush.RectFilled(a.Bounds().X1, a.Bounds().Y1, a.Bounds().X2, a.Bounds().Y2, a.FillColor())
		brush.Rect(a.Bounds().X1, a.Bounds().Y1, a.Bounds().X2, a.Bounds().Y2, a.BorderColor())
	}

	r.overlay.FlushAreas()
}
