package tools

import (
	"sdmm/util"
)

// Tool is a basic interface for tools in the panel.
type Tool interface {
	Name() string

	AltBehaviour() bool
	setAltBehaviour(bool)

	// Goes when user clicks on the map.
	onStart(coord util.Point)
	// Goes when user clicked and, while holding the mouse button, move the mouse.
	onMove(coord util.Point)
	// Goes when user releases the mouse button.
	onStop(coord util.Point)
}

// Tool is a basic interface for tools in the panel.
type tool struct {
	altBehaviour bool
}

func (t *tool) AltBehaviour() bool {
	return t.altBehaviour
}

func (t *tool) setAltBehaviour(altBehaviour bool) {
	t.altBehaviour = altBehaviour
}

func (t *tool) onStart(util.Point) {
}

func (t *tool) onMove(util.Point) {
}

func (t *tool) onStop(util.Point) {
}
