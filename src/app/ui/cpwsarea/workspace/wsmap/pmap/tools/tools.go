package tools

import (
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

const (
	TNAdd    = "Add"
	TNFill   = "Fill"
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
	UpdateCanvasByCoord(util.Point)
	SelectedPrefab() (*dmmprefab.Prefab, bool)
	CommitChanges(string)

	MarkEditedTile(util.Point)
	MarkEditedArea(util.Bounds)
	MarkDeletedTile(util.Point)

	SelectInstance(i *dmminstance.Instance)
	DeleteInstance(i *dmminstance.Instance)

	DeleteHoveredTile(commit bool)
	HoveredInstance() *dmminstance.Instance
}

var selectedToolName = TNAdd

func SetSelected(toolName string) {
	selectedToolName = toolName
}

func IsSelected(toolName string) bool {
	return selectedToolName == toolName
}

type Tools struct {
	canvasControl canvasControl
	canvasState   canvasState

	active   bool
	oldCoord util.Point

	tools map[string]Tool

	startedTool Tool
}

func (t *Tools) SetCanvasControl(canvasControl canvasControl) {
	t.canvasControl = canvasControl
}

func (t *Tools) SetCanvasState(canvasState canvasState) {
	t.canvasState = canvasState
}

func (t *Tools) Selected() Tool {
	return t.tools[selectedToolName]
}

func (t *Tools) Tools() map[string]Tool {
	return t.tools
}

func New(editor editor) *Tools {
	tools := map[string]Tool{
		TNAdd:    newAdd(editor),
		TNFill:   newFill(editor),
		TNPick:   newPick(editor),
		TNDelete: newDelete(editor),
	}
	return &Tools{
		tools: tools,
	}
}

func (t *Tools) Process(altBehaviour bool) {
	if t.active && t.startedTool != t.Selected() {
		t.startedTool.onStop(t.oldCoord)
	}

	t.Selected().process()
	t.Selected().setAltBehaviour(altBehaviour)
	t.processSelectedToolStart()
	t.processSelectedToolsStop()
}

func (t *Tools) OnMouseMove() {
	t.processSelectedToolMove()
}

func (t *Tools) processSelectedToolStart() {
	if !t.canvasState.HoverOutOfBounds() {
		if t.canvasControl.Dragging() && !t.active {
			t.startedTool = t.Selected()
			t.Selected().onStart(t.canvasState.HoveredTile())
			t.active = true
		}
	}
}

func (t *Tools) processSelectedToolMove() {
	if !t.canvasState.HoverOutOfBounds() {
		coord := t.canvasState.HoveredTile()
		if coord != t.oldCoord && t.active {
			t.Selected().onMove(coord)
		}
		t.oldCoord = coord
	}
}

func (t *Tools) processSelectedToolsStop() {
	if !t.canvasControl.Dragging() && t.active {
		t.Selected().onStop(t.oldCoord)
		t.active = false
	}
}
