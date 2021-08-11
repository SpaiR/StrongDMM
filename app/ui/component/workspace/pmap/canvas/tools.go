package canvas

import (
	"github.com/SpaiR/imgui-go"
	tool2 "github.com/SpaiR/strongdmm/app/ui/component/workspace/pane_map/canvas/tool"
)

type ToolsAction interface {
	tool2.AddAction
}

type Tools struct {
	control *Control
	state   *State

	active     bool
	oldX, oldY int

	selected tool2.Tool
	add      tool2.Tool
}

func NewTools(action ToolsAction, control *Control, state *State) *Tools {
	tools := &Tools{
		control: control,
		state:   state,

		add: tool2.NewAdd(action),
	}

	state.onHoverChangeListeners = append(state.onHoverChangeListeners, tools.processSelectedToolMove)

	return tools
}

func (t *Tools) Process() {
	t.process()
	t.showControls()
}

func (t *Tools) process() {
	// TODO: normal selection
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
	x, y := t.state.HoveredTile()
	if (x != t.oldX || y != t.oldY) && t.active {
		t.selected.OnMove(x, y)
	}
	t.oldX, t.oldY = x, y
}

func (t *Tools) processSelectedToolsStop() {
	if !t.control.dragging && t.active {
		t.selected.OnStop(t.oldX, t.oldY)
		t.active = false
	}
}

func (t *Tools) showControls() {
	imgui.Button("A")
}
