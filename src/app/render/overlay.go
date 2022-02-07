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

type AreaBorder interface {
	Borders() []util.Bounds
	Color() util.Color
}

type overlay interface {
	Areas() []OverlayArea
	FlushAreas()

	Units() map[uint64]HighlightUnit
	FlushUnits()

	AreasBorders() []AreaBorder
	FlushAreasBorders()
}

// Draw overlays for aras borders.
func (r *Render) batchOverlayAreasBorders() {
	if r.overlay == nil {
		return
	}

	for _, areaBorder := range r.overlay.AreasBorders() {
		for _, bounds := range areaBorder.Borders() {
			brush.Line(bounds.X1, bounds.Y1, bounds.X2, bounds.Y2, areaBorder.Color())
		}
	}

	r.overlay.FlushAreasBorders()
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
