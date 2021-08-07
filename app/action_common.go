package app

import (
	"github.com/SpaiR/imgui-go"
	"github.com/SpaiR/strongdmm/app/command"
)

func (a *app) WindowCond() imgui.Condition {
	return a.tmpWindowCond
}

func (a *app) IsWindowReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

func (a *app) PointSize() float32 {
	return a.masterWindow.PointSize
}

func (a *app) PointSizePtr() *float32 {
	return &a.masterWindow.PointSize
}

func (a *app) MasterWindowSize() (int, int) {
	return a.masterWindow.Handle.GetSize()
}

func (a *app) RunLater(job func()) {
	a.masterWindow.RunLater(job)
}

func (a *app) AddMouseChangeCallback(cb func(uint, uint)) int {
	return a.masterWindow.AddMouseChangeCallback(cb)
}

func (a *app) RemoveMouseChangeCallback(id int) {
	a.masterWindow.RemoveMouseChangeCallback(id)
}

func (a *app) SetCommandStack(id string) {
	a.commandStorage.SetStack(id)
}

func (a *app) PushCommand(command command.Command) {
	a.commandStorage.Push(command)
}

func (a *app) HasUndo() bool {
	return a.commandStorage.HasUndo()
}

func (a *app) HasRedo() bool {
	return a.commandStorage.HasRedo()
}
