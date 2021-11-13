package pmap

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

var (
	oColEmpty = util.Color{}

	oColHoverTileFill   = util.MakeColor(1, 1, 1, 0.25)
	oColHoverTileBorder = util.MakeColor(1, 1, 1, 1)

	oColDeleteTileFile   = util.MakeColor(1, 0, 0, 0.25)
	oColDeleteTileBorder = util.MakeColor(1, 0, 0, 1)

	oColEditTileBorder    = util.MakeColor(0, 1, 0, 1)
	oColDeletedTileBorder = util.MakeColor(1, 0, 0, 1)

	oColSelectInstance = util.MakeColor(0, 1, 0, 1)
	oColDeleteInstance = util.MakeColor(1, 0, 0, 1)
)

func (p *PaneMap) processCanvasOverlay() {
	if tools.IsSelected(tools.TNSelect) || tools.IsSelected(tools.TNDelete) {
		hoveredInstance := p.canvasState.HoveredInstance()

		if tools.IsSelected(tools.TNSelect) {
			p.pushUnitHighlight(hoveredInstance, oColSelectInstance)
		} else {
			if p.tools.Selected().AltBehaviour() {
				if !p.canvasState.HoverOutOfBounds() {
					p.pushAreaHover(p.canvasState.HoveredTileBounds(), oColDeleteTileFile, oColDeleteTileBorder)
				}
			} else {
				p.pushUnitHighlight(hoveredInstance, oColDeleteInstance)
			}
		}
	} else if !p.canvasState.HoverOutOfBounds() {
		p.pushAreaHover(p.canvasState.HoveredTileBounds(), oColHoverTileFill, oColHoverTileBorder)
	}

	for _, bounds := range p.editor.editedAreas {
		p.pushAreaHover(bounds, oColEmpty, oColEditTileBorder)
	}

	for _, bounds := range p.editor.deletedAreas {
		p.pushAreaHover(bounds, oColEmpty, oColDeletedTileBorder)
	}
}

func (p *PaneMap) pushUnitHighlight(instance *dmminstance.Instance, color util.Color) {
	if instance != nil {
		p.canvasOverlay.PushUnit(canvas.HighlightUnit{
			Id_:    instance.Id(),
			Color_: color,
		})
	}
}

func (p *PaneMap) pushAreaHover(bounds util.Bounds, fillColor, borderColor util.Color) {
	p.canvasOverlay.PushArea(canvas.OverlayArea{
		Bounds_:      bounds,
		FillColor_:   fillColor,
		BorderColor_: borderColor,
	})
}
