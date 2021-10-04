package canvas

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/component/workspace/pmap/canvas/tool"
	"sdmm/util"
)

type ToolsAction interface {
	tool.AddAction
}

type Tools struct {
	control *Control
	state   *State

	active   bool
	oldCoord util.Point

	selected tool.Tool
	add      tool.Tool
}

func NewTools(action ToolsAction, control *Control, state *State) *Tools {
	tools := &Tools{
		control: control,
		state:   state,
	}

	tools.add = tool.NewAdd(action, tools)

	state.onHoverChangeListeners = append(state.onHoverChangeListeners, tools.processSelectedToolMove)

	return tools
}

func (t *Tools) Process() {
	t.process()
	t.showControls()
}

func (t *Tools) process() {
	// FIXME: normal selection
	t.selected = t.add

	t.processSelectedToolStart()
	t.processSelectedToolsStop()
}

func (t *Tools) processSelectedToolStart() {
	if !t.state.HoverOutOfBounds() {
		if t.control.dragging && !t.active {
			t.selected.OnStart(t.state.HoveredTile())
			t.active = true
		}
	}
}

func (t *Tools) processSelectedToolMove() {
	coord := t.state.HoveredTile()
	if coord != t.oldCoord && t.active {
		t.selected.OnMove(coord)
	}
	t.oldCoord = coord
}

func (t *Tools) processSelectedToolsStop() {
	if !t.control.dragging && t.active {
		t.selected.OnStop(t.oldCoord)
		t.active = false
	}
}

func (t *Tools) showControls() {
	imgui.Button("A")
}
