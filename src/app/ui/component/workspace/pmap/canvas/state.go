package canvas

import (
	"sdmm/util"
)

type State struct {
	hoveredTile      util.Point // DMM coord system: starts from 1, position in tiles.
	hoveredTilePoint util.Point // Absolute coord system: starts from 0, position in pixels.

	iconSize   int
	maxX, maxY int

	onHoverChangeListeners []func()
}

func NewState(maxX, maxY, iconSize int) *State {
	return &State{
		maxX:     maxX,
		maxY:     maxY,
		iconSize: iconSize,
	}
}

func (s *State) SetHoveredTile(relLocalX, relLocalY, zLevel int) {
	// We are out of bounds for sure.
	if relLocalX < 0 || relLocalY < 0 || zLevel < 0 {
		s.hoveredTile = util.Point{}
		s.hoveredTilePoint = util.Point{}
		return
	}

	// Mouse position coords, but local to the tiles.
	localMouseX := relLocalX / s.IconSize()
	localMouseY := relLocalY / s.IconSize()

	// Local coords, but adjusted to DMM coord system.
	mapMouseX := localMouseX + 1
	mapMouseY := localMouseY + 1

	s.hoveredTile = util.Point{X: mapMouseX, Y: mapMouseY, Z: zLevel}
	s.hoveredTilePoint = util.Point{X: localMouseX * s.IconSize(), Y: localMouseY * s.IconSize(), Z: zLevel}

	for _, listener := range s.onHoverChangeListeners {
		listener()
	}
}

func (s State) IconSize() int {
	return s.iconSize
}

func (s State) HoveredTile() util.Point {
	return s.hoveredTile
}

func (s State) HoveredTilePoint() (x, y float32) {
	return float32(s.hoveredTilePoint.X), float32(s.hoveredTilePoint.Y)
}

func (s State) HoverOutOfBounds() bool {
	return s.hoveredTile.X < 1 || s.hoveredTile.Y < 1 || s.hoveredTile.X > s.maxX || s.hoveredTile.Y > s.maxY
}
