package layout

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/config"
	"sdmm/app/ui/cpenvironment"
	"sdmm/app/ui/cpprefabs"
	"sdmm/app/ui/cpsearch"
	"sdmm/app/ui/cpvareditor"
	"sdmm/app/ui/cpwsarea"
)

type app interface {
	cpenvironment.App
	cpprefabs.App
	cpsearch.App
	cpwsarea.App
	cpvareditor.App

	ConfigRegister(config.Config)
	ConfigFind(name string) config.Config
	ConfigSaveV(config.Config)

	IsLayoutReset() bool
}

type Layout struct {
	cpenvironment.Environment
	cpprefabs.Prefabs
	cpsearch.Search
	cpwsarea.WsArea
	cpvareditor.VarEditor

	app app

	initialized bool

	leftNodeId      int32
	leftUpNodeId    int32
	leftDownNodeId  int32
	centerNodeId    int32
	rightNodeId     int32
	rightUpNodeId   int32
	rightDownNodeId int32

	tmpNextShowNode  string
	tmpNextFocusNode string
}

func New(app app) *Layout {
	l := &Layout{app: app}
	l.loadConfig()
	l.Environment.Init(app)
	l.Prefabs.Init(app)
	l.Search.Init(app)
	l.WsArea.Init(app)
	l.VarEditor.Init(app)
	return l
}

func (l *Layout) Process() {
	l.updateNodes()

	l.showEnvironmentNode()
	l.showPrefabsNode()
	l.showSearchNode()
	l.showVariablesNode()
	l.showWorkspaceAreaNode() // The latest node will have a focus by default

	l.initialized = true

	l.tmpNextShowNode = ""
	l.tmpNextFocusNode = ""
}

// CheckLayoutState checks if there was a change in the layout state.
func (l *Layout) CheckLayoutState() bool {
	return configState != l.config().State
}

// SyncLayoutState syncs the layout state from the configState const and the configuration file.
func (l *Layout) SyncLayoutState() {
	cfg := l.config()
	cfg.State = configState
	l.app.ConfigSaveV(cfg)
	log.Println("[layout] layout state updated:", configState)
}

func (l *Layout) ShowNode(nodeName string) {
	imgui.ExtSetDockTabSelected(nodeName)
	l.tmpNextShowNode = nodeName
}

func (l *Layout) FocusNode(nodeName string) {
	l.tmpNextFocusNode = nodeName
}

const (
	NodeNameEnvironment   = "Environment"
	NodeNameWorkspaceArea = "Workspace Area"
	NodeNamePrefabs       = "Prefabs"
	NodeNameSearch        = "Search"
	NodeNameVariables     = "Variables"
)

func (l *Layout) showEnvironmentNode() {
	l.wrapNode(NodeNameEnvironment, int(l.leftNodeId), l.Environment.Process)
}

func (l *Layout) showWorkspaceAreaNode() {
	l.wrapNodeV(NodeNameWorkspaceArea, int(l.centerNodeId), false, false, false, l.WsArea.Process)
}

func (l *Layout) showPrefabsNode() {
	l.wrapNode(NodeNamePrefabs, int(l.rightUpNodeId), l.Prefabs.Process)
}

func (l *Layout) showSearchNode() {
	l.wrapNode(NodeNameSearch, int(l.rightUpNodeId), l.Search.Process)
}

func (l *Layout) showVariablesNode() {
	l.wrapNode(NodeNameVariables, int(l.rightDownNodeId), l.VarEditor.Process)
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
	l.wrapNodeV(id, nodeId, true, true, false, content)
}

const defaultWindowFlags = imgui.WindowFlagsNone

func (l *Layout) wrapNodeV(id string, nodeId int, addPadding, showTabBar, closable bool, content func()) {
	if l.app.IsLayoutReset() {
		imgui.DockBuilderDockWindow(id, nodeId)
	}

	if !addPadding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}

	if id == l.tmpNextShowNode {
		imgui.SetNextWindowCollapsed(false, imgui.ConditionOnce)
	}
	if id == l.tmpNextFocusNode {
		imgui.SetNextWindowFocus()
	}

	if imgui.BeginV(id, nil, defaultWindowFlags) {
		if !addPadding {
			imgui.PopStyleVar()
		}
		if imgui.IsWindowDocked() {
			l.tweakWindowNode(showTabBar, closable)
		}
		content()
	} else if !addPadding {
		imgui.PopStyleVar()
	}
	imgui.End()
}

// Tweak window node flags and nodes ordering.
func (l *Layout) tweakWindowNode(showTabBar bool, closable bool) {
	if dockNode := imgui.DockBuilderGetNode(imgui.GetWindowDockID()); dockNode != 0 {
		var flags imgui.DockNodeFlags

		if !closable {
			flags |= imgui.DockNodeFlagsNoCloseButton
		}

		if !showTabBar {
			flags |= imgui.DockNodeFlagsNoTabBar
		} else {
			flags |= imgui.DockNodeFlagsNoWindowMenuButton
		}

		dockNode.SetLocalFlags(int(flags))

		if !l.initialized {
			dockNode.ExtSelectFirstTab() // Ensure that after the launch windows will be selected properly
		}
	}
}
