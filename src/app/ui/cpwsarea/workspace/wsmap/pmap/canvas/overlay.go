package canvas

import (
	"sdmm/app/render"
	"sdmm/util"
)

type OverlayArea struct {
	Bounds_      util.Bounds
	FillColor_   util.Color
	BorderColor_ util.Color
}

func (o OverlayArea) Bounds() util.Bounds {
	return o.Bounds_
}

func (o OverlayArea) FillColor() util.Color {
	return o.FillColor_
}

func (o OverlayArea) BorderColor() util.Color {
	return o.BorderColor_
}

type Overlay struct {
	areas []render.OverlayArea
}

func NewOverlay() *Overlay {
	return &Overlay{}
}

func (o *Overlay) PushArea(tile render.OverlayArea) {
	o.areas = append(o.areas, tile)
}

func (o *Overlay) Areas() []render.OverlayArea {
	return o.areas
}

func (o *Overlay) Flush() {
	o.areas = nil
}
