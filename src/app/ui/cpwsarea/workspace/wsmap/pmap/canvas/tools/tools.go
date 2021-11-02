package tools

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas/tools/tool"
	"sdmm/util"
)

type canvasControl interface {
	Dragging() bool
	SelectionMode() bool
}

type canvasState interface {
	tool.Visuals

	AddHoverChangeListener(func())
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

func NewTools(toolModify tool.Modify, canvasControl canvasControl, canvasState canvasState) *Tools {
	tools := &Tools{
		canvasControl: canvasControl,
		canvasState:   canvasState,

		add: tool.NewAdd(toolModify, canvasState),
	}

	canvasState.AddHoverChangeListener(tools.processSelectedToolMove)

	return tools
}

func (t *Tools) Process() {
	t.showControls()
	if !t.canvasControl.SelectionMode() {
		t.process()
	}
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

func (t *Tools) showControls() {
	imgui.Button("A")
}
