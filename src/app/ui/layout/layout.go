package layout

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpenvironment"
	"sdmm/app/ui/cpprefabs"
	"sdmm/app/ui/cpvareditor"
	"sdmm/app/ui/cpwsarea"
)

type app interface {
	cpenvironment.App
	cpprefabs.App
	cpwsarea.App
	cpvareditor.App

	IsWindowReset() bool
}

type Layout struct {
	cpenvironment.Environment
	cpprefabs.Prefabs
	cpwsarea.WsArea
	cpvareditor.VarEditor

	app app

	leftNodeId     int32
	leftUpNodeId   int32
	leftDownNodeId int32
	centerNodeId   int32
	rightNodeId    int32
}

func New(app app) *Layout {
	l := &Layout{app: app}
	l.Environment.Init(app)
	l.Prefabs.Init(app)
	l.WsArea.Init(app)
	l.VarEditor.Init(app)
	return l
}

func (l *Layout) Process() {
	l.updateNodes()

	l.showLeftUpNode()
	l.showLeftDownNode()
	l.showRightNode()
	l.showCenterNode() // The latest node will have a focus by default
}

func (l *Layout) showLeftUpNode() {
	wrapNode("Environment##leftUpNode", int(l.leftUpNodeId), l.Environment.Process)
}

func (l *Layout) showLeftDownNode() {
	wrapNode("Prefabs##leftDownNode", int(l.leftDownNodeId), l.Prefabs.Process)
}

func (l *Layout) showCenterNode() {
	wrapNodeV("centerNode", int(l.centerNodeId), false, false, l.WsArea.Process)
}

func (l *Layout) showRightNode() {
	wrapNode("Variables##rightNode", int(l.rightNodeId), l.VarEditor.Process)
}

func (l *Layout) updateNodes() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	if !l.app.IsWindowReset() {
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
	wrapNodeV(id, nodeId, true, true, content)
}

func wrapNodeV(id string, nodeId int, addPadding, showTabBar bool, content func()) {
	imgui.DockBuilderDockWindow(id, nodeId)
	if !addPadding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}
	if imgui.BeginV(id, nil, imgui.WindowFlagsNoMove) {
		if !addPadding {
			imgui.PopStyleVar()
		}
		makeNodeNotAttachable(showTabBar)
		content()
	} else if !addPadding {
		imgui.PopStyleVar()
	}
	imgui.End()
}

// Remove all node decorations, so window will become none attachable.
func makeNodeNotAttachable(showTabBar bool) {
	flags := imgui.DockNodeFlagsNoCloseButton | imgui.DockNodeFlagsNoDocking | imgui.DockNodeFlagsNoDockingSplitMe

	if !showTabBar {
		flags |= imgui.DockNodeFlagsNoTabBar
	} else {
		flags |= imgui.DockNodeFlagsNoWindowMenuButton
	}

	imgui.DockBuilderGetNode(imgui.GetWindowDockID()).SetLocalFlags(flags)
}
