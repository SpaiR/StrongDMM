package pmap

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
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
		colTileFill = overlay.ColorToolAddTileFill
		if !p.tools.Selected().AltBehaviour() {
			colTileBorder = overlay.ColorToolAddTileBorder
		} else {
			colTileBorder = overlay.ColorToolAddAltTileBorder
		}
	case tools.TNFill:
		if !p.tools.Selected().AltBehaviour() {
			colTileFill = overlay.ColorToolFillTileFill
		} else {
			colTileFill = overlay.ColorToolFillAltTileFill
		}
	case tools.TNPick:
		colInstance = overlay.ColorToolPickInstance
	case tools.TNDelete:
		if !p.tools.Selected().AltBehaviour() {
			colInstance = overlay.ColorToolDeleteInstance
		} else {
			colTileFill = overlay.ColorToolDeleteAltTileFill
			colTileBorder = overlay.ColorToolDeleteAltTileBorder
		}
	}

	if colInstance != overlay.ColorEmpty {
		p.pushUnitHighlight(p.canvasState.HoveredInstance(), colInstance)
	}
	if !p.canvasState.HoverOutOfBounds() {
		p.pushAreaHover(p.canvasState.HoveredTileBounds(), colTileFill, colTileBorder)
	}
}

func (p *PaneMap) processCanvasOverlayFlick() {
	for idx, a := range p.editor.flickAreas {
		delta := imgui.Time() - a.time
		col := flickColor(overlay.ColorFlickTileFill, delta)

		if delta < flickDurationSec {
			p.pushAreaHover(a.area, col, overlay.ColorEmpty)
		} else {
			p.editor.flickAreas = append(p.editor.flickAreas[:idx], p.editor.flickAreas[idx+1:]...)
		}
	}

	for idx, i := range p.editor.flickInstance {
		delta := imgui.Time() - i.time
		col := flickColor(overlay.ColorFlickInstance, delta)

		if delta < flickDurationSec {
			p.pushUnitHighlight(i.instance, col)
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
