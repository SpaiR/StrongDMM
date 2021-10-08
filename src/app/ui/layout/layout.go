package layout

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpenvironment"
	"sdmm/app/ui/cpinstances"
	"sdmm/app/ui/cpwsarea"
)

type action interface {
	cpenvironment.Action
	cpinstances.Action
	cpwsarea.Action

	AppIsWindowReset() bool
}

type Layout struct {
	cpenvironment.Environment
	cpinstances.Instances
	cpwsarea.WsArea

	action action

	leftNodeId     int32
	leftUpNodeId   int32
	leftDownNodeId int32
	centerNodeId   int32
	rightNodeId    int32
}

func New(a action) *Layout {
	l := &Layout{action: a}
	l.Environment.Init(a)
	l.Instances.Init(a)
	l.WsArea.Init(a)
	return l
}

func (l *Layout) Process() {
	l.updateNodes()

	l.showLeftUpNode()
	l.showLeftDownNode()
	l.showCenterNode()
	l.showRightNode()
}

func (l *Layout) showLeftUpNode() {
	wrapNode("leftUpNode", int(l.leftUpNodeId), l.Environment.Process)
}

func (l *Layout) showLeftDownNode() {
	wrapNode("leftDownNode", int(l.leftDownNodeId), l.Instances.Process)
}

func (l *Layout) showCenterNode() {
	wrapNodeV("centerNode", int(l.centerNodeId), false, l.WsArea.Process)
}

func (l *Layout) showRightNode() {
	wrapNode("rightNode", int(l.rightNodeId), func() {
		imgui.Text("Placeholder")
	})
}

func (l *Layout) updateNodes() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	if !l.action.AppIsWindowReset() {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

	l.centerNodeId = int32(dockSpaceId)

	size := imgui.MainViewport().Size()

	imgui.DockBuilderSetNodeSize(int(l.centerNodeId), size)
	imgui.DockBuilderSplitNode(int(l.centerNodeId), imgui.DirLeft, .2, &l.leftNodeId, &l.centerNodeId)
	imgui.DockBuilderSplitNode(int(l.leftNodeId), imgui.DirUp, .5, &l.leftUpNodeId, &l.leftDownNodeId)
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
