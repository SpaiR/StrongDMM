package canvas

import (
	"sdmm/util"
)

type State struct {
	hoveredTile       util.Point  // DMM coord system: starts from 1, position in tiles.
	hoveredTileBounds util.Bounds // Absolute coord system: starts from 0, position in pixels.

	modifiedTiles []util.Bounds

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

func (s *State) SetHoveredTile(relLocalX, relLocalY, level int) {
	// We are out of bounds for sure.
	if relLocalX < 0 || relLocalY < 0 || level < 0 {
		s.hoveredTile = util.Point{}
		s.hoveredTileBounds = util.Bounds{}
		return
	}

	// Mouse position coords, but local to the tiles.
	localMouseX := relLocalX / s.iconSize
	localMouseY := relLocalY / s.iconSize

	// Local coords, but adjusted to DMM coord system.
	mapMouseX := localMouseX + 1
	mapMouseY := localMouseY + 1

	s.hoveredTile = util.Point{X: mapMouseX, Y: mapMouseY, Z: level}

	s.hoveredTileBounds = util.Bounds{
		X1: float32(localMouseX * s.iconSize),
		Y1: float32(localMouseY * s.iconSize),
		X2: float32(localMouseX*s.iconSize + s.iconSize),
		Y2: float32(localMouseY*s.iconSize + s.iconSize),
	}

	for _, listener := range s.onHoverChangeListeners {
		listener()
	}
}

func (s *State) AddModifiedTile(coord util.Point) {
	s.modifiedTiles = append(s.modifiedTiles, util.Bounds{
		X1: float32((coord.X - 1) * s.iconSize),
		Y1: float32((coord.Y - 1) * s.iconSize),
		X2: float32((coord.X-1)*s.iconSize + s.iconSize),
		Y2: float32((coord.Y-1)*s.iconSize + s.iconSize),
	})
}

func (s *State) ResetModifiedTiles() {
	s.modifiedTiles = nil
}

func (s State) HoveredTile() util.Point {
	return s.hoveredTile
}

func (s State) HoveredTileBounds() util.Bounds {
	return s.hoveredTileBounds
}

func (s State) HoverOutOfBounds() bool {
	return s.hoveredTile.X < 1 || s.hoveredTile.Y < 1 || s.hoveredTile.X > s.maxX || s.hoveredTile.Y > s.maxY
}

func (s State) ModifiedTiles() []util.Bounds {
	return s.modifiedTiles
}
