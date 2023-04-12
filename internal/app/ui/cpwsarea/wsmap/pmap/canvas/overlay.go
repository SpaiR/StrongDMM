package canvas

import (
	"sdmm/internal/app/render"
	"sdmm/internal/util"
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
	areas        []render.OverlayArea
	units        map[uint64]render.HighlightUnit
	areasBorders []render.AreaBorder
}

func NewOverlay() *Overlay {
	return &Overlay{
		units: make(map[uint64]render.HighlightUnit),
	}
}

func (o *Overlay) PushArea(area OverlayArea) {
	o.areas = append(o.areas, area)
}

func (o *Overlay) Areas() []render.OverlayArea {
	return o.areas
}

func (o *Overlay) FlushAreas() {
	o.areas = o.areas[:0]
}

type HighlightUnit struct {
	Id_    uint64
	Color_ util.Color
}

func (o *Overlay) PushUnit(unit HighlightUnit) {
	o.units[unit.Id()] = unit
}

func (o *Overlay) Units() map[uint64]render.HighlightUnit {
	return o.units
}

func (o *Overlay) FlushUnits() {
	for id := range o.units {
		delete(o.units, id)
	}
}

func (h HighlightUnit) Id() uint64 {
	return h.Id_
}

func (h HighlightUnit) Color() util.Color {
	return h.Color_
}

type OverlayAreaBorder struct {
	Borders_ []util.Bounds
	Color_   util.Color
}

func (o OverlayAreaBorder) Borders() []util.Bounds {
	return o.Borders_
}

func (o OverlayAreaBorder) Color() util.Color {
	return o.Color_
}

func (o *Overlay) PushAreaBorder(areaBorder OverlayAreaBorder) {
	o.areasBorders = append(o.areasBorders, areaBorder)
}

func (o *Overlay) AreasBorders() []render.AreaBorder {
	return o.areasBorders
}

func (o *Overlay) FlushAreasBorders() {
	o.areasBorders = o.areasBorders[:0]
}
