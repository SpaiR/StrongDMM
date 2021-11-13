package pmap

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

var (
	oColEmpty = util.Color{}

	oColToolAddTileFill   = util.MakeColor(1, 1, 1, 0.25)
	oColToolAddTileBorder = util.MakeColor(1, 1, 1, 1)

	oColToolSelectInstance = util.MakeColor(0, 1, 0, 1)

	oColToolDeleteInstance      = util.MakeColor(1, 0, 0, 1)
	oColToolDeleteAltTileFill   = util.MakeColor(1, 0, 0, 0.25)
	oColToolDeleteAltTileBorder = util.MakeColor(1, 0, 0, 1)

	oColEditTileBorder    = util.MakeColor(0, 1, 0, 1)
	oColDeletedTileBorder = util.MakeColor(1, 0, 0, 1)
)

func (p *PaneMap) processCanvasOverlay() {
	p.processCanvasOverlayTools()
	p.processCanvasOverlayAreas()
}

func (p *PaneMap) processCanvasOverlayTools() {
	var (
		colInstance   util.Color
		colTileFill   util.Color
		colTileBorder util.Color
	)

	switch p.tools.Selected().Name() {
	case tools.TNAdd:
		colTileFill = oColToolAddTileFill
		colTileBorder = oColToolAddTileBorder
	case tools.TNSelect:
		colInstance = oColToolSelectInstance
	case tools.TNDelete:
		if !p.tools.Selected().AltBehaviour() {
			colInstance = oColToolDeleteInstance
		} else {
			colTileFill = oColToolDeleteAltTileFill
			colTileBorder = oColToolDeleteAltTileBorder
		}
	}

	if colInstance != oColEmpty {
		p.pushUnitHighlight(p.canvasState.HoveredInstance(), colInstance)
	}
	if !p.canvasState.HoverOutOfBounds() {
		p.pushAreaHover(p.canvasState.HoveredTileBounds(), colTileFill, colTileBorder)
	}
}

func (p *PaneMap) processCanvasOverlayAreas() {
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
