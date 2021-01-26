package ui

import (
	"github.com/SpaiR/imgui-go"
)

type layoutAction interface {
	WindowCond() imgui.Condition
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
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.GetMainViewport(), imgui.DockNodeFlagsNone)

	if l.action.WindowCond() != imgui.ConditionAlways {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

	imgui.DockBuilderSetNodeSize(dockSpaceId, imgui.GetMainViewport().GetSize())
	imgui.DockBuilderSplitNode(dockSpaceId, imgui.DirLeft, .2, &l.LeftNodeId, &l.CenterNodeId)

	imgui.DockBuilderFinish(dockSpaceId)
}
