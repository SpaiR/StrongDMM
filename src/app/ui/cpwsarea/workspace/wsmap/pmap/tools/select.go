package tools

import (
	"math"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/overlay"
	"sdmm/dmapi/dmmap"
	"sdmm/util"
)

type tSelectMode int

const (
	tSelectModeSelectArea tSelectMode = iota
	tSelectModeMoveArea
)

type tSelect struct {
	tool

	fillStart    util.Point
	fillAreaInit util.Bounds
	fillArea     util.Bounds

	initTiles []dmmap.Tile
	prevTiles []dmmap.Tile

	startMovePoint util.Point

	dragging bool

	mode tSelectMode
}

func (tSelect) Name() string {
	return TNSelect
}

func (t *tSelect) Reset() {
	t.fillStart = util.Point{}
	t.fillAreaInit = util.Bounds{}
	t.fillArea = util.Bounds{}

	t.initTiles = nil
	t.prevTiles = nil
}

func newSelect() *tSelect {
	return &tSelect{}
}

func (t *tSelect) Stale() bool {
	return !t.dragging
}

func (tSelect) AltBehaviour() bool {
	return false
}

func (t *tSelect) process() {
	if t.active() {
		ed.OverlayPushArea(t.fillArea, overlay.ColorToolSelectTileFill, overlay.ColorToolSelectTileBorder)
	}
}

func (t *tSelect) onStart(coord util.Point) {
	t.dragging = true

	switch t.mode {
	case tSelectModeSelectArea:
		t.startSelectArea(coord)
	case tSelectModeMoveArea:
		t.startMoveArea(coord)
	}
}

func (t *tSelect) startSelectArea(coord util.Point) {
	t.Reset()
	t.fillStart = coord
	t.onMove(coord)
}

func (t *tSelect) startMoveArea(coord util.Point) {
	if t.fillArea.Contains(float32(coord.X), float32(coord.Y)) {
		t.startMovePoint = coord
		t.initTiles = collectTiles(ed.Dmm(), t.fillArea, t.fillStart.Z)
	} else {
		t.mode = tSelectModeSelectArea
		t.onStart(coord)
	}
}

func (t *tSelect) onMove(coord util.Point) {
	if !t.active() {
		return
	}

	switch t.mode {
	case tSelectModeSelectArea:
		t.selectArea(coord)
	case tSelectModeMoveArea:
		t.moveArea(coord)
	}
}

func (t *tSelect) selectArea(coord util.Point) {
	t.fillArea.X1 = float32(math.Min(float64(t.fillStart.X), float64(coord.X)))
	t.fillArea.Y1 = float32(math.Min(float64(t.fillStart.Y), float64(coord.Y)))
	t.fillArea.X2 = float32(math.Max(float64(t.fillStart.X), float64(coord.X)))
	t.fillArea.Y2 = float32(math.Max(float64(t.fillStart.Y), float64(coord.Y)))
	t.fillAreaInit = t.fillArea
}

func (t *tSelect) moveArea(coord util.Point) {
	dmm := ed.Dmm()

	shift := coord.Minus(t.startMovePoint)
	nextArea := t.fillAreaInit.Plus(float32(shift.X), float32(shift.Y))

	if nextArea.X1 <= 0 || nextArea.Y1 <= 0 || int(nextArea.X2) > dmm.MaxX || int(nextArea.Y2) > dmm.MaxY {
		return
	}

	t.fillArea = nextArea

	var updateCoords []util.Point

	// Clear moved tiles (they're moved tho...)
	for _, initTile := range t.initTiles {
		updateCoords = append(updateCoords, initTile.Coord)
		ed.TileDelete(initTile.Coord)
	}

	// Restore previous tiles content (tiles we've moved through)
	for _, prevTile := range t.prevTiles {
		updateCoords = append(updateCoords, prevTile.Coord)
		ed.TileReplace(prevTile.Coord, prevTile.Instances().Prefabs())
	}

	// Move a content to a new place
	for _, initTile := range t.initTiles {
		nextTilePoint := initTile.Coord.Plus(shift)

		if !dmm.HasTile(initTile.Coord) {
			continue
		}

		tile := dmm.GetTile(nextTilePoint)
		t.prevTiles = append(t.prevTiles, tile.Copy())
		updateCoords = append(updateCoords, tile.Coord)

		ed.TileReplace(nextTilePoint, initTile.Instances().Prefabs())
	}

	ed.UpdateCanvasByCoords(updateCoords)
}

func (t *tSelect) onStop(util.Point) {
	if !t.active() {
		return
	}

	switch t.mode {
	case tSelectModeSelectArea:
		t.stopSelectArea()
	case tSelectModeMoveArea:
		t.stopMoveArea()
	}

	t.dragging = false
}

func (t *tSelect) stopSelectArea() {
	t.mode = tSelectModeMoveArea
}

func (t *tSelect) stopMoveArea() {
	t.initTiles = collectTiles(ed.Dmm(), t.fillArea, t.fillStart.Z)
	t.fillAreaInit = t.fillArea
	go ed.CommitChanges("Move Selected Area")
}

func (t *tSelect) onDeselect() {
	t.Reset()
}

func (t *tSelect) active() bool {
	return !t.fillStart.Equals(0, 0, 0)
}

func collectTiles(dmm *dmmap.Dmm, area util.Bounds, zLevel int) (tiles []dmmap.Tile) {
	for x := area.X1; x <= area.X2; x++ {
		for y := area.Y1; y <= area.Y2; y++ {
			coord := util.Point{X: int(x), Y: int(y), Z: zLevel}
			tiles = append(tiles, dmm.GetTile(coord).Copy())
		}
	}
	return tiles
}
