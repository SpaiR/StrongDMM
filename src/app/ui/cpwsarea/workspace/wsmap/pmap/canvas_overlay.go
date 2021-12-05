package pmap

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

var (
	oColEmpty = util.Color{}

	oColToolAddTileFill      = util.MakeColor(1, 1, 1, 0.25)
	oColToolAddTileBorder    = util.MakeColor(1, 1, 1, 1)
	oColToolAddAltTileBorder = util.MakeColor(0, 1, 0, 1)

	oColToolSelectInstance = util.MakeColor(0, 1, 0, 1)

	oColToolDeleteInstance      = util.MakeColor(1, 0, 0, 1)
	oColToolDeleteAltTileFill   = util.MakeColor(1, 0, 0, 0.25)
	oColToolDeleteAltTileBorder = util.MakeColor(1, 0, 0, 1)

	oColEditTileBorder    = util.MakeColor(0, 1, 0, 1)
	oColDeletedTileBorder = util.MakeColor(1, 0, 0, 1)

	oColFlickTileFill = util.MakeColor(1, 1, 1, 1)
	oColFlickInstance = util.MakeColor(0, 1, 0, 1)
)

const flickDurationSec = .5

type flickArea struct {
	time float64
	area util.Bounds
}

type flickInstance struct {
	time     float64
	instance *dmminstance.Instance
}

func (p *PaneMap) processCanvasOverlay() {
	p.processCanvasOverlayTools()
	p.processCanvasOverlayAreas()
	p.processCanvasOverlayFlick()
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
		if !p.tools.Selected().AltBehaviour() {
			colTileBorder = oColToolAddTileBorder
		} else {
			colTileBorder = oColToolAddAltTileBorder
		}
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
	for _, a := range p.editor.editedAreas {
		p.pushAreaHover(a, oColEmpty, oColEditTileBorder)
	}

	for _, a := range p.editor.deletedAreas {
		p.pushAreaHover(a, oColEmpty, oColDeletedTileBorder)
	}
}

func (p *PaneMap) processCanvasOverlayFlick() {
	for idx, a := range p.editor.flickAreas {
		delta := imgui.Time() - a.time
		col := flickColor(oColFlickTileFill, delta)

		if delta < flickDurationSec {
			p.pushAreaHover(a.area, col, oColEmpty)
		} else {
			p.editor.flickAreas = append(p.editor.flickAreas[:idx], p.editor.flickAreas[idx+1:]...)
		}
	}

	for idx, i := range p.editor.flickInstance {
		delta := imgui.Time() - i.time

		if delta < flickDurationSec {
			p.pushUnitHighlight(i.instance, oColFlickInstance)
		} else {
			p.editor.flickInstance = append(p.editor.flickInstance[:idx], p.editor.flickInstance[idx+1:]...)
		}
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

func flickColor(col util.Color, delta float64) util.Color {
	return util.MakeColor(
		col.R(),
		col.G(),
		col.B(),
		col.A()-float32(delta/flickDurationSec),
	)
}
