package tools

import (
	"math"

	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/overlay"
	"sdmm/internal/dmapi/dmmap/dmmdata"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/util"

	"github.com/rs/zerolog/log"
)

type tSelectMode int

const (
	tSelectModeSelectArea tSelectMode = iota
	tSelectModeMoveArea
)

// ToolGrab can be used to select a specific tiles area and to manipulate the selected area state.
// Tool works in two modes:
//  1. Select the area
//  2. Move the area
//
// The first one is available when no area selected or user selects the area outside the currently selected.
// The second mode is activated automatically when dragging mouse on the currently selected area.
//
// Copy/Paste operations will automatically use selected area for them.
type ToolGrab struct {
	tool

	fillStart    util.Point
	fillAreaInit util.Bounds
	fillArea     util.Bounds

	initTiles []dmmap.Tile
	prevTiles map[util.Point]dmmdata.Prefabs

	startMovePoint util.Point

	dragging bool

	mode tSelectMode
}

func (ToolGrab) Name() string {
	return TNGrab
}

func (t *ToolGrab) Bounds() util.Bounds {
	return t.fillArea
}

func (t *ToolGrab) HasSelectedArea() bool {
	return t.fillStart != util.Point{}
}

func (t *ToolGrab) Reset() {
	t.fillStart = util.Point{}
	t.fillAreaInit = util.Bounds{}
	t.fillArea = util.Bounds{X1: math.MaxFloat32, Y1: math.MaxFloat32}

	t.initTiles = nil
	t.prevTiles = nil

	log.Print("grab tools reset")
}

func newGrab() *ToolGrab {
	return &ToolGrab{
		fillArea: util.Bounds{X1: math.MaxFloat32, Y1: math.MaxFloat32},
	}
}

func (t *ToolGrab) Stale() bool {
	return !t.dragging
}

func (ToolGrab) AltBehaviour() bool {
	return false
}

func (t *ToolGrab) SelectArea(tiles []util.Point) {
	if len(tiles) == 0 {
		return
	}

	t.fillStart = tiles[0]
	for _, tile := range tiles {
		t.selectArea(float64(t.fillArea.X1), float64(t.fillArea.Y1), float64(t.fillArea.X2), float64(t.fillArea.Y2), tile)
	}
	t.stopMoveArea()
}

func (t *ToolGrab) PreSelectArea(tiles []util.Point) {
	t.prevTiles = make(map[util.Point]dmmdata.Prefabs, len(tiles))
	for _, tile := range tiles {
		if ed.Dmm().HasTile(tile) {
			t.prevTiles[tile] = ed.Dmm().GetTile(tile).Instances().Prefabs().Copy()
		}
	}
}

func (t *ToolGrab) process() {
	if t.active() {
		ed.OverlayPushArea(t.fillArea, overlay.ColorToolSelectTileFill, overlay.ColorToolSelectTileBorder)
	}
}

func (t *ToolGrab) onStart(coord util.Point) {
	t.dragging = true

	switch t.mode {
	case tSelectModeSelectArea:
		t.startSelectArea(coord)
	case tSelectModeMoveArea:
		t.startMoveArea(coord)
	}
}

func (t *ToolGrab) startSelectArea(coord util.Point) {
	t.Reset()
	t.fillStart = coord
	t.onMove(coord)
}

func (t *ToolGrab) startMoveArea(coord util.Point) {
	if t.fillArea.Contains(float32(coord.X), float32(coord.Y)) {
		t.startMovePoint = coord
	} else {
		t.mode = tSelectModeSelectArea
		t.onStart(coord)
	}
}

func (t *ToolGrab) onMove(coord util.Point) {
	if !t.active() {
		return
	}

	switch t.mode {
	case tSelectModeSelectArea:
		x, y := float64(t.fillStart.X), float64(t.fillStart.Y)
		t.selectArea(x, y, x, y, coord)
	case tSelectModeMoveArea:
		t.moveArea(coord)
	}
}

func (t *ToolGrab) selectArea(minX, minY, maxX, maxY float64, coord util.Point) {
	t.fillArea.X1 = float32(math.Min(minX, float64(coord.X)))
	t.fillArea.Y1 = float32(math.Min(minY, float64(coord.Y)))
	t.fillArea.X2 = float32(math.Max(maxX, float64(coord.X)))
	t.fillArea.Y2 = float32(math.Max(maxY, float64(coord.Y)))
	t.fillAreaInit = t.fillArea
}

func (t *ToolGrab) moveArea(coord util.Point) {
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
	for tile, prevTile := range t.prevTiles {
		updateCoords = append(updateCoords, tile)
		ed.TileReplace(tile, prevTile)
	}

	// Move a content to a new place
	for _, initTile := range t.initTiles {
		nextTilePoint := initTile.Coord.Plus(shift)

		if !dmm.HasTile(initTile.Coord) {
			continue
		}

		tile := dmm.GetTile(nextTilePoint)
		if _, ok := t.prevTiles[tile.Coord]; !ok {
			t.prevTiles[tile.Coord] = tile.Instances().Prefabs().Copy()
		}
		updateCoords = append(updateCoords, tile.Coord)

		ed.TileReplace(nextTilePoint, initTile.Instances().Prefabs())
	}

	ed.UpdateCanvasByCoords(updateCoords)
}

func (t *ToolGrab) onStop(util.Point) {
	if !t.active() {
		return
	}

	switch t.mode {
	case tSelectModeSelectArea:
		t.stopSelectArea()
	case tSelectModeMoveArea:
		t.stopMoveArea()
		go ed.CommitChanges("Move Grabbed Area")
	}

	t.dragging = false
}

func (t *ToolGrab) stopSelectArea() {
	t.mode = tSelectModeMoveArea
	t.initTiles = collectTiles(ed.Dmm(), t.fillArea, t.fillStart.Z)
	t.prevTiles = make(map[util.Point]dmmdata.Prefabs)
}

func (t *ToolGrab) stopMoveArea() {
	t.initTiles = collectTiles(ed.Dmm(), t.fillArea, t.fillStart.Z)
	t.fillAreaInit = t.fillArea
}

func (t *ToolGrab) OnDeselect() {
	t.Reset()
}

func (t *ToolGrab) active() bool {
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
