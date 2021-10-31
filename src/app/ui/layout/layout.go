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

	IsLayoutReset() bool
}

// Version returns the current version of the layout. Need to be updated after any major layout changes.
func Version() uint {
	return 1
}

type Layout struct {
	cpenvironment.Environment
	cpprefabs.Prefabs
	cpwsarea.WsArea
	cpvareditor.VarEditor

	app app

	leftNodeId      int32
	leftUpNodeId    int32
	leftDownNodeId  int32
	centerNodeId    int32
	rightNodeId     int32
	rightUpNodeId   int32
	rightDownNodeId int32
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

	l.showEnvironmentNode()
	l.showPrefabsNode()
	l.showVariablesNode()
	l.showWorkspaceAreaNode() // The latest node will have a focus by default
}

func (l *Layout) showEnvironmentNode() {
	l.wrapNode("Environment", int(l.leftNodeId), l.Environment.Process)
}

func (l *Layout) showWorkspaceAreaNode() {
	l.wrapNodeV("Workspace Area", int(l.centerNodeId), false, false, l.WsArea.Process)
}

func (l *Layout) showPrefabsNode() {
	l.wrapNode("Prefabs", int(l.rightUpNodeId), l.Prefabs.Process)
}

func (l *Layout) showVariablesNode() {
	l.wrapNode("Variables", int(l.rightDownNodeId), l.VarEditor.Process)
}

const (
	ratioL2C   = .2 // The left panel
	ratioLU2LD = .5 // The left-top and the left-bottom panels
	ratioR2C   = .2 // The right panel
	ratioRU2RD = .5 // The right-top and the right-bottom panels
)

func (l *Layout) updateNodes() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	if !l.app.IsLayoutReset() {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

	l.centerNodeId = int32(dockSpaceId)

	size := imgui.MainViewport().Size()

	imgui.DockBuilderSetNodeSize(int(l.centerNodeId), size)
	imgui.DockBuilderSplitNode(int(l.centerNodeId), imgui.DirLeft, ratioL2C, &l.leftNodeId, &l.centerNodeId)
	l.leftNodeId = int32(imgui.DockBuilderSplitNode(int(l.leftNodeId), imgui.DirUp, ratioLU2LD, &l.leftUpNodeId, &l.leftDownNodeId))
	imgui.DockBuilderSetNodeSize(int(l.centerNodeId), size)
	imgui.DockBuilderSplitNode(int(l.centerNodeId), imgui.DirRight, ratioR2C, &l.rightNodeId, &l.centerNodeId)
	l.rightNodeId = int32(imgui.DockBuilderSplitNode(int(l.rightNodeId), imgui.DirUp, ratioRU2RD, &l.rightUpNodeId, &l.rightDownNodeId))

	imgui.DockBuilderFinish(dockSpaceId)
}

func (l *Layout) wrapNode(id string, nodeId int, content func()) {
	l.wrapNodeV(id, nodeId, true, true, content)
}

const defaultWindowFlags = imgui.WindowFlagsNone

func (l *Layout) wrapNodeV(id string, nodeId int, addPadding, showTabBar bool, content func()) {
	if l.app.IsLayoutReset() {
		imgui.DockBuilderDockWindow(id, nodeId)
	}

	if !addPadding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}
	if imgui.BeginV(id, nil, defaultWindowFlags) {
		if !addPadding {
			imgui.PopStyleVar()
		}
		if imgui.IsWindowDocked() {
			tweakWindowDockFlags(showTabBar)
		}
		content()
	} else if !addPadding {
		imgui.PopStyleVar()
	}
	imgui.End()
}

const defaultNodeFlags = imgui.DockNodeFlagsNoCloseButton

// Remove all node decorations, so window will become none attachable.
func tweakWindowDockFlags(showTabBar bool) {
	flags := defaultNodeFlags

	if !showTabBar {
		flags |= imgui.DockNodeFlagsNoTabBar
	} else {
		flags |= imgui.DockNodeFlagsNoWindowMenuButton
	}

	imgui.DockBuilderGetNode(imgui.GetWindowDockID()).SetLocalFlags(flags)
}
