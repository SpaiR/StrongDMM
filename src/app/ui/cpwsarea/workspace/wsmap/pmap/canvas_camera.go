package pmap

import (
	"github.com/SpaiR/imgui-go"
)

const scaleFactor float32 = 1.5

func (p *PaneMap) processCanvasCamera() {
	p.processCameraMove()
	p.processCameraZoom()
}

func (p *PaneMap) processCameraMove() {
	if p.canvasControl.Moving() {
		if delta := imgui.CurrentIO().MouseDelta(); delta.X != 0 || delta.Y != 0 {
			camera := p.canvas.Render().Camera()
			camera.Translate(delta.X/camera.Scale, -delta.Y/camera.Scale)
		}
	}
}

func (p *PaneMap) processCameraZoom() {
	if !p.canvasControl.Zoomed() {
		return
	}

	camera := p.canvas.Render().Camera()

	_, mouseWheel := imgui.CurrentIO().MouseWheel()

	zoomIn := mouseWheel > 0
	scale := camera.Scale

	if zoomIn {
		scale *= -scaleFactor
	}

	mousePos := imgui.MousePos()
	localPos := mousePos.Minus(p.canvasControl.PosMin())

	offsetX := localPos.X / scale / 2
	offsetY := (p.size.Y - localPos.Y) / scale / 2

	camera.Translate(offsetX, offsetY)
	camera.Zoom(zoomIn, scaleFactor)
}
