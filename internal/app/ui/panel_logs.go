package ui

import (
	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/pkg/util"
	w "github.com/SpaiR/strongdmm/pkg/widget"
)

type logsAction interface {
	WindowCond() imgui.Condition
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

	util.ImGuiSetNextWindowCentered(imgui.Vec2{X: 300, Y: 300}, l.action.WindowCond())
	w.Window("Logs", w.Layout{

	}).Open(&l.open).Build()
}
