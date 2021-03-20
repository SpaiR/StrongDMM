package ui

import (
	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/ui/component"
)

type layoutAction interface {
	component.EnvironmentAction
	component.WorkspaceAreaAction

	IsWindowReset() bool
	PointSizePtr() *float32
}

type Layout struct {
	component.Environment
	component.WorkspaceArea

	action layoutAction

	leftNodeId   int32
	centerNodeId int32
	rightNodeId  int32
}

func NewLayout(a layoutAction) *Layout {
	l := &Layout{action: a}
	l.Environment.Init(a)
	l.WorkspaceArea.Init(a)
	return l
}

func (l *Layout) Process() {
	l.updateNodes()

	l.showLeftNode()
	l.showCenterNode()
	l.showRightNode()
}

func (l *Layout) showLeftNode() {
	wrapNode("leftNode", int(l.leftNodeId), l.Environment.Process)
}

func (l *Layout) showCenterNode() {
	wrapNodeV("centerNode", int(l.centerNodeId), false, l.WorkspaceArea.Process)
}

func (l *Layout) showRightNode() {
	wrapNode("rightNode", int(l.rightNodeId), func() {
		imgui.Text("placeholder")
	})
}

func (l *Layout) updateNodes() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	if !l.action.IsWindowReset() {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

	l.centerNodeId = int32(dockSpaceId)

	size := imgui.MainViewport().Size()

	imgui.DockBuilderSetNodeSize(int(l.centerNodeId), size)
	imgui.DockBuilderSplitNode(int(l.centerNodeId), imgui.DirLeft, .2, &l.leftNodeId, &l.centerNodeId)
	imgui.DockBuilderSetNodeSize(int(l.centerNodeId), size)
	imgui.DockBuilderSplitNode(int(l.centerNodeId), imgui.DirRight, .2, &l.rightNodeId, &l.centerNodeId)

	imgui.DockBuilderFinish(dockSpaceId)
}

func wrapNode(id string, nodeId int, content func()) {
	wrapNodeV(id, nodeId, true, content)
}

func wrapNodeV(id string, nodeId int, padding bool, content func()) {
	imgui.DockBuilderDockWindow(id, nodeId)
	if !padding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}
	imgui.BeginV(id, nil, imgui.WindowFlagsNoMove)
	if !padding {
		imgui.PopStyleVar()
	}
	makeNodeNotAttachable()
	content()
	imgui.End()
}

// Remove all node decorations, so window will become none attachable.
func makeNodeNotAttachable() {
	imgui.DockBuilderGetNode(imgui.GetWindowDockID()).SetLocalFlags(
		imgui.DockNodeFlagsNoTabBar | imgui.DockNodeFlagsNoCloseButton |
			imgui.DockNodeFlagsNoDocking | imgui.DockNodeFlagsNoDockingSplitMe)
}
