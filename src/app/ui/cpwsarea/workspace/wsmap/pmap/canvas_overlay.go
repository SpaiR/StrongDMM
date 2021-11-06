package pmap

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/util"
)

var (
	overlayColorHoveredTileFill   = util.MakeColor(1, 1, 1, 0.25)
	overlayColorHoveredTileBorder = util.MakeColor(1, 1, 1, 1)
	overlayColorEditedTileBorder  = util.MakeColor(0, 1, 0, 1)
)

func (p *PaneMap) processCanvasOverlay() {
	if !p.canvasState.HoverOutOfBounds() {
		p.canvasOverlay.PushArea(canvas.OverlayArea{
			Bounds_:      p.canvasState.HoveredTileBounds(),
			FillColor_:   overlayColorHoveredTileFill,
			BorderColor_: overlayColorHoveredTileBorder,
		})
	}

	for _, editedTilesBounds := range p.editor.editedAreas {
		p.canvasOverlay.PushArea(canvas.OverlayArea{
			Bounds_:      editedTilesBounds,
			BorderColor_: overlayColorEditedTileBorder,
		})
	}
}
