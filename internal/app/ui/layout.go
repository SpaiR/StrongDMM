package ui

import (
	"github.com/SpaiR/imgui-go"
)

type layoutAction interface {
	IsWindowReset() bool
}

type Layout struct {
	action layoutAction

	LeftNodeId   int32
	CenterNodeId int32
}

func NewLayout(action layoutAction) *Layout {
	return &Layout{
		action: action,
	}
}

func (l *Layout) Process() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	if !l.action.IsWindowReset() {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

	imgui.DockBuilderSetNodeSize(dockSpaceId, imgui.MainViewport().Size())
	imgui.DockBuilderSplitNode(dockSpaceId, imgui.DirLeft, .2, &l.LeftNodeId, &l.CenterNodeId)

	imgui.DockBuilderFinish(dockSpaceId)
}
