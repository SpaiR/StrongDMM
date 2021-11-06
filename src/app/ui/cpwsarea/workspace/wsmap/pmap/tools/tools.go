package tools

import (
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools/tool"
	"sdmm/util"
)

type canvasControl interface {
	Dragging() bool
	SelectionMode() bool
}

type canvasState interface {
	HoverOutOfBounds() bool
	HoveredTile() util.Point
}

type Tools struct {
	canvasControl canvasControl
	canvasState   canvasState

	active   bool
	oldCoord util.Point

	selected tool.Tool
	add      tool.Tool
}

func (t *Tools) SetCanvasControl(canvasControl canvasControl) {
	t.canvasControl = canvasControl
}

func (t *Tools) SetCanvasState(canvasState canvasState) {
	t.canvasState = canvasState
}

func New(editor tool.Editor) *Tools {
	return &Tools{
		add: tool.NewAdd(editor),
	}
}

func (t *Tools) Process() {
	if !t.canvasControl.SelectionMode() {
		t.process()
	}
}

func (t *Tools) OnMouseMove() {
	t.processSelectedToolMove()
}

func (t *Tools) process() {
	// FIXME: normal selection
	t.selected = t.add

	t.processSelectedToolStart()
	t.processSelectedToolsStop()
}

func (t *Tools) processSelectedToolStart() {
	if !t.canvasState.HoverOutOfBounds() {
		if t.canvasControl.Dragging() && !t.active {
			t.selected.OnStart(t.canvasState.HoveredTile())
			t.active = true
		}
	}
}

func (t *Tools) processSelectedToolMove() {
	coord := t.canvasState.HoveredTile()
	if coord != t.oldCoord && t.active {
		t.selected.OnMove(coord)
	}
	t.oldCoord = coord
}

func (t *Tools) processSelectedToolsStop() {
	if !t.canvasControl.Dragging() && t.active {
		t.selected.OnStop(t.oldCoord)
		t.active = false
	}
}
