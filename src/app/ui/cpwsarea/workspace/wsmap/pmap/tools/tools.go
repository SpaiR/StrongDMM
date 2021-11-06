package tools

import (
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

const (
	TNAdd    = "Add"
	TNSelect = "Select"
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
	UpdateCanvasByCoord(coord util.Point)
	SelectedPrefab() (*dmmprefab.Prefab, bool)
	CommitChanges(string)
	MarkEditedTile(coord util.Point)
	ClearEditedTiles()
	SelectInstance(i *dmminstance.Instance)
	HoveredInstance() *dmminstance.Instance
}

type Tools struct {
	canvasControl canvasControl
	canvasState   canvasState

	active   bool
	oldCoord util.Point

	selected Tool

	tools map[string]Tool
}

func (t *Tools) SetCanvasControl(canvasControl canvasControl) {
	t.canvasControl = canvasControl
}

func (t *Tools) SetCanvasState(canvasState canvasState) {
	t.canvasState = canvasState
}

func (t *Tools) SetSelectedByName(toolName string) {
	t.selected = t.tools[toolName]
}

func (t *Tools) SetSelected(tool Tool) {
	t.selected = tool
}

func (t *Tools) Selected() Tool {
	return t.selected
}

func (t *Tools) Tools() map[string]Tool {
	return t.tools
}

func (t Tools) IsSelected(toolName string) bool {
	return t.selected.Name() == toolName
}

func New(editor editor) *Tools {
	tools := map[string]Tool{
		TNAdd:    newAdd(editor),
		TNSelect: newSelect(editor),
	}
	return &Tools{
		selected: tools[TNAdd],
		tools:    tools,
	}
}

func (t *Tools) Process(altBehaviour bool) {
	t.selected.setAltBehaviour(altBehaviour)
	t.processSelectedToolStart()
	t.processSelectedToolsStop()
}

func (t *Tools) OnMouseMove() {
	t.processSelectedToolMove()
}

func (t *Tools) processSelectedToolStart() {
	if !t.canvasState.HoverOutOfBounds() {
		if t.canvasControl.Dragging() && !t.active {
			t.selected.onStart(t.canvasState.HoveredTile())
			t.active = true
		}
	}
}

func (t *Tools) processSelectedToolMove() {
	if !t.canvasState.HoverOutOfBounds() {
		coord := t.canvasState.HoveredTile()
		if coord != t.oldCoord && t.active {
			t.selected.onMove(coord)
		}
		t.oldCoord = coord
	}
}

func (t *Tools) processSelectedToolsStop() {
	if !t.canvasControl.Dragging() && t.active {
		t.selected.onStop(t.oldCoord)
		t.active = false
	}
}
