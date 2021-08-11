package app

import (
	"github.com/SpaiR/imgui-go"
	"github.com/SpaiR/strongdmm/app/command"
)

func (a *app) AppWindowCond() imgui.Condition {
	return a.tmpWindowCond
}

func (a *app) AppIsWindowReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

func (a *app) AppPointSize() float32 {
	return a.masterWindow.PointSize
}

func (a *app) AppPointSizePtr() *float32 {
	return &a.masterWindow.PointSize
}

func (a *app) AppMasterWindowSize() (int, int) {
	return a.masterWindow.Handle.GetSize()
}

func (a *app) AppRunLater(job func()) {
	a.masterWindow.AppRunLater(job)
}

func (a *app) AppAddMouseChangeCallback(cb func(uint, uint)) int {
	return a.masterWindow.AddMouseChangeCallback(cb)
}

func (a *app) AppRemoveMouseChangeCallback(id int) {
	a.masterWindow.RemoveMouseChangeCallback(id)
}

func (a *app) AppSetCommandStack(id string) {
	a.commandStorage.SetStack(id)
}

func (a *app) AppPushCommand(command command.Command) {
	a.commandStorage.Push(command)
}

func (a *app) AppHasUndo() bool {
	return a.commandStorage.HasUndo()
}

func (a *app) AppHasRedo() bool {
	return a.commandStorage.HasRedo()
}
