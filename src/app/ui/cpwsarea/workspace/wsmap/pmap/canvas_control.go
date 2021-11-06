package pmap

import "log"

func (p *PaneMap) updateCanvasMousePosition(mouseX, mouseY int) {
	// If canvas itself is not active, then no need to search for mouse position at all.
	if !p.canvasControl.Active() {
		p.canvasState.SetMousePosition(-1, -1, -1)
		return
	}

	// Mouse position relative to canvas.
	relMouseX := float32(mouseX - int(p.canvasControl.PosMin().X))
	relMouseY := float32(mouseY - int(p.canvasControl.PosMin().Y))

	// Canvas height itself.
	canvasHeight := p.canvasControl.PosMax().Y - p.canvasControl.PosMin().Y

	// Mouse position by Y axis, but with bottom-up orientation.
	relMouseY = canvasHeight - relMouseY

	// Transformed coordinates with respect of camera scale and shift.
	camera := p.canvas.Render().Camera()
	relMouseX = relMouseX/camera.Scale - (camera.ShiftX)
	relMouseY = relMouseY/camera.Scale - (camera.ShiftY)

	p.canvasState.SetMousePosition(int(relMouseX), int(relMouseY), p.activeLevel)
}

func (p *PaneMap) processCanvasHoveredInstance() {
	if !p.canvasControl.SelectionMode() || p.canvasState.HoverOutOfBounds() {
		p.tmpLastHoveredInstance = nil
	}
	p.canvasState.SetHoveredInstance(p.tmpLastHoveredInstance)
}

func (p *PaneMap) selectHoveredInstance() {
	if hoveredInstance := p.canvasState.HoveredInstance(); hoveredInstance != nil && p.canvasControl.SelectionMode() {
		log.Println("[pmap] selected hovered instance:", hoveredInstance.Id())
		p.SelectInstance(hoveredInstance)
	}
}
