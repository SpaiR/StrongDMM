package tools

import (
	"log"

	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

const (
	TNAdd    = "Add"
	TNFill   = "Fill"
	TNSelect = "Select"
	TNPick   = "Pick"
	TNDelete = "Delete"
)

type canvasControl interface {
	Dragging() bool
}

type canvasState interface {
	HoverOutOfBounds() bool
	HoveredTile() util.Point
}

type editor interface {
	Dmm() *dmmap.Dmm

	CommitChanges(commitMsg string)

	UpdateCanvasByCoords([]util.Point)
	UpdateCanvasByTiles([]dmmap.Tile)

	SelectedPrefab() (*dmmprefab.Prefab, bool)

	OverlayPushTile(coord util.Point, colFill, colBorder util.Color)
	OverlayPushArea(area util.Bounds, colFill, colBorder util.Color)

	InstanceSelect(i *dmminstance.Instance)
	InstanceDelete(i *dmminstance.Instance)

	TileReplace(coord util.Point, prefabs dmmdata.Prefabs)

	TileDeleteSelected()
	TileDelete(util.Point)
	HoveredInstance() *dmminstance.Instance
}

var (
	cc canvasControl
	cs canvasState
	ed editor

	active   bool
	oldCoord util.Point

	tools = map[string]Tool{
		TNAdd:    newAdd(),
		TNFill:   newFill(),
		TNSelect: newSelect(),
		TNPick:   newPick(),
		TNDelete: newDelete(),
	}

	selectedToolName = TNAdd

	startedTool Tool
)

func SetSelected(toolName string) Tool {
	if selectedToolName != toolName {
		log.Println("[tools] selecting:", toolName)
		tools[selectedToolName].OnDeselect()
		selectedToolName = toolName
	}
	return Selected()
}

func IsSelected(toolName string) bool {
	return selectedToolName == toolName
}

func SetEditor(editor editor) {
	ed = editor
}

func SetCanvasControl(canvasControl canvasControl) {
	cc = canvasControl
}

func SetCanvasState(canvasState canvasState) {
	cs = canvasState
}

func Selected() Tool {
	return tools[selectedToolName]
}

func Tools() map[string]Tool {
	return tools
}

func Process(altBehaviour bool) {
	if active && startedTool != Selected() {
		startedTool.onStop(oldCoord)
	}

	Selected().process()
	Selected().setAltBehaviour(altBehaviour)
	processSelectedToolStart()
	processSelectedToolsStop()
}

func OnMouseMove() {
	processSelectedToolMove()
}

func SelectedTiles() []util.Point {
	if selectTool, ok := Selected().(*ToolSelect); ok {
		if len(selectTool.initTiles) > 0 {
			tiles := make([]util.Point, 0, len(selectTool.initTiles))
			for _, tile := range selectTool.initTiles {
				tiles = append(tiles, tile.Coord)
			}
			return tiles
		}
	}
	return []util.Point{cs.HoveredTile()}
}

func processSelectedToolStart() {
	if !cs.HoverOutOfBounds() {
		if cc.Dragging() && !active {
			startedTool = Selected()
			Selected().onStart(cs.HoveredTile())
			active = true
		}
	}
}

func processSelectedToolMove() {
	if !cs.HoverOutOfBounds() {
		coord := cs.HoveredTile()
		if coord != oldCoord && active {
			Selected().onMove(coord)
		}
		oldCoord = coord
	}
}

func processSelectedToolsStop() {
	if !cc.Dragging() && active {
		Selected().onStop(oldCoord)
		active = false
	}
}
