package app

import (
	"github.com/SpaiR/imgui-go"
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
