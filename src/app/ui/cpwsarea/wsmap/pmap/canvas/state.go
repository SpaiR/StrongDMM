package canvas

import (
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type State struct {
	hoveredTile       util.Point  // DMM coord system: starts from 1, position in tiles.
	lastHoveredTile   util.Point  // Same as the hoveredTile, but always stores the latest tile which was hovered.
	hoveredTileBounds util.Bounds // Absolute coord system: starts from 0, position in pixels.

	hoveredInstance *dmminstance.Instance

	relMouseX, relMouseY int

	iconSize   int
	maxX, maxY int
}

func (s *State) SetHoveredInstance(hoveredInstance *dmminstance.Instance) {
	s.hoveredInstance = hoveredInstance
}

func NewState(maxX, maxY, iconSize int) *State {
	return &State{
		maxX:     maxX,
		maxY:     maxY,
		iconSize: iconSize,
	}
}

func (s *State) SetMousePosition(relMouseX, relMouseY, level int) {
	s.relMouseX, s.relMouseY = relMouseX, relMouseY

	// We are out of bounds for sure.
	if relMouseX < 0 || relMouseY < 0 || level < 0 {
		s.hoveredTile = util.Point{}
		s.hoveredTileBounds = util.Bounds{}
		return
	}

	// Mouse position coords, but local to the tiles.
	localMouseX := relMouseX / s.iconSize
	localMouseY := relMouseY / s.iconSize

	// Local coords, but adjusted to DMM coord system.
	mapMouseX := localMouseX + 1
	mapMouseY := localMouseY + 1

	s.hoveredTile = util.Point{X: mapMouseX, Y: mapMouseY, Z: level}
	s.lastHoveredTile = s.hoveredTile.Copy()

	s.hoveredTileBounds = util.Bounds{
		X1: float32(localMouseX * s.iconSize),
		Y1: float32(localMouseY * s.iconSize),
		X2: float32(localMouseX*s.iconSize + s.iconSize),
		Y2: float32(localMouseY*s.iconSize + s.iconSize),
	}
}

func (s *State) HoveredInstance() *dmminstance.Instance {
	return s.hoveredInstance
}

func (s State) RelMouseX() int {
	return s.relMouseX
}

func (s State) RelMouseY() int {
	return s.relMouseY
}

func (s State) HoveredTile() util.Point {
	return s.hoveredTile
}

func (s State) LastHoveredTile() util.Point {
	return s.lastHoveredTile
}

func (s State) HoveredTileBounds() util.Bounds {
	return s.hoveredTileBounds
}

func (s State) HoverOutOfBounds() bool {
	return s.hoveredTile.X < 1 || s.hoveredTile.Y < 1 || s.hoveredTile.X > s.maxX || s.hoveredTile.Y > s.maxY
}
