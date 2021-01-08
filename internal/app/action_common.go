package app

import "github.com/SpaiR/imgui-go"

func (a *app) WindowCond() imgui.Condition {
	return a.tmpWindowCond
}

func (a *app) PointSize() float32 {
	return a.masterWindow.PointSize
}

func (a *app) CenterNodeId() int {
	return int(a.uiLayout.CenterNodeId)
}

func (a *app) LeftNodeId() int {
	return int(a.uiLayout.LeftNodeId)
}
