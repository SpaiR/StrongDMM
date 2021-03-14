package app

import (
	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmenv"
)

func (a *app) WindowCond() imgui.Condition {
	return a.tmpWindowCond
}

func (a *app) IsWindowReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

func (a *app) LoadedEnvironment() *dmenv.Dme {
	return a.loadedEnvironment
}

func (a *app) HasLoadedEnvironment() bool {
	return a.loadedEnvironment != nil
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