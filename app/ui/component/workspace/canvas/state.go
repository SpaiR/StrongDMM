package canvas

type State struct {
	hoveredTile      point // DMM coord system: starts from 1, position in tiles.
	hoveredTilePoint point // Absolute coord system: starts from 0, position in pixels.

	iconSize   int
	maxX, maxY int
}

func NewState(maxX, maxY, iconSize int) *State {
	return &State{
		maxX:     maxX,
		maxY:     maxY,
		iconSize: iconSize,
	}
}

func (s *State) SetHoveredTile(relLocalX, relLocalY float32) {
	// We are out of bounds for sure.
	if relLocalX < 0 || relLocalY < 0 {
		s.hoveredTile = point{}
		s.hoveredTilePoint = point{}
		return
	}

	// Mouse position coords, but local to the tiles.
	localMouseX := int(relLocalX) / s.IconSize()
	localMouseY := int(relLocalY) / s.IconSize()

	// Local coords, but adjusted to DMM coord system.
	mapMouseX := localMouseX + 1
	mapMouseY := localMouseY + 1

	s.hoveredTile = point{x: mapMouseX, y: mapMouseY}
	s.hoveredTilePoint = point{localMouseX * s.IconSize(), localMouseY * s.IconSize()}
}

func (s State) IconSize() int {
	return s.iconSize
}

func (s State) HoveredTilePoint() (x, y float32) {
	return float32(s.hoveredTilePoint.x), float32(s.hoveredTilePoint.y)
}

func (s State) HoverOutOfBounds() bool {
	return s.hoveredTile.x < 1 || s.hoveredTile.y < 1 || s.hoveredTile.x > s.maxX || s.hoveredTile.y > s.maxY
}

type point struct {
	x, y int
}
