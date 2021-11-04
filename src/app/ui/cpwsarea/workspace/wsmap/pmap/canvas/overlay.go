package canvas

import (
	"sdmm/app/render"
	"sdmm/util"
)

type OverlayTile struct {
	Bounds_      util.Bounds
	FillColor_   util.Color
	BorderColor_ util.Color
}

func (o OverlayTile) Bounds() util.Bounds {
	return o.Bounds_
}

func (o OverlayTile) FillColor() util.Color {
	return o.FillColor_
}

func (o OverlayTile) BorderColor() util.Color {
	return o.BorderColor_
}

type Overlay struct {
	tiles []render.OverlayTile
}

func NewOverlay() *Overlay {
	return &Overlay{}
}

func (o *Overlay) PushTile(tile render.OverlayTile) {
	o.tiles = append(o.tiles, tile)
}

func (o *Overlay) Tiles() []render.OverlayTile {
	return o.tiles
}

func (o *Overlay) Flush() {
	o.tiles = nil
}
