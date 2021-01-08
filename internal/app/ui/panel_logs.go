package ui

import (
	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/pkg/util"
	w "github.com/SpaiR/strongdmm/pkg/widget"
)

type logsAction interface {
	WindowCond() imgui.Condition
	PointSize() float32
}

type Logs struct {
	action logsAction
	open   bool
}

func NewLogs(action logsAction) *Logs {
	return &Logs{
		action: action,
	}
}

func (l *Logs) Open() {
	l.open = true
}

func (l *Logs) Process() {
	if !l.open {
		return
	}

	size := imgui.Vec2{X: 300 * l.action.PointSize(), Y: 300 * l.action.PointSize()}
	util.ImGuiSetNextWindowCentered(size, l.action.WindowCond())
	w.Window("Logs", w.Layout{

	}).Open(&l.open).Build()
}
