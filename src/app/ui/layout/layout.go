package layout

import (
	"log"

	"sdmm/app/config"
	"sdmm/app/ui/cpenvironment"
	"sdmm/app/ui/cpprefabs"
	"sdmm/app/ui/cpsearch"
	"sdmm/app/ui/cpvareditor"
	"sdmm/app/ui/cpwsarea"
	"sdmm/app/ui/layout/lnode"
	"sdmm/util/slice"

	"github.com/SpaiR/imgui-go"
)

type app interface {
	cpenvironment.App
	cpprefabs.App
	cpsearch.App
	cpwsarea.App
	cpvareditor.App

	ConfigRegister(config.Config)
	ConfigFind(name string) config.Config

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

	tmpNextShowNode  []string
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

	l.tmpNextShowNode = nil
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
	log.Println("[layout] layout state updated:", configState)
}

func (l *Layout) ShowNode(nodeName string) {
	imgui.ExtSetDockTabSelected(nodeName)
	l.tmpNextShowNode = append(l.tmpNextShowNode, nodeName)
}

func (l *Layout) FocusNode(nodeName string) {
	l.tmpNextFocusNode = nodeName
}

func (l *Layout) showEnvironmentNode() {
	l.wrapNode(lnode.NameEnvironment, int(l.leftNodeId), l.Environment.Process)
}

func (l *Layout) showWorkspaceAreaNode() {
	l.wrapNodeV(lnode.NameWorkspaceArea, int(l.centerNodeId), func() {
		l.WsArea.Process(int(l.centerNodeId))
	}, wrapCfg{
		noWindow: true,
	})
}

func (l *Layout) showPrefabsNode() {
	l.wrapNode(lnode.NamePrefabs, int(l.rightUpNodeId), l.Prefabs.Process)
}

func (l *Layout) showSearchNode() {
	l.wrapNode(lnode.NameSearch, int(l.rightUpNodeId), l.Search.Process)
}

func (l *Layout) showVariablesNode() {
	l.wrapNode(lnode.NameVariables, int(l.rightDownNodeId), l.VarEditor.Process)
}

const (
	ratioL2C   = .2 // The left panel
	ratioLU2LD = .5 // The left-top and the left-bottom panels
	ratioR2C   = .2 // The right panel
	ratioRU2RD = .5 // The right-top and the right-bottom panels
)

func (l *Layout) updateNodes() {
	dockSpaceId := imgui.DockSpaceOverViewportV(imgui.MainViewport(), imgui.DockNodeFlagsNone)

	// We only need a center node ID for sure.
	l.centerNodeId = int32(dockSpaceId)

	if !l.app.IsLayoutReset() {
		return
	}

	imgui.DockBuilderRemoveNode(dockSpaceId)
	imgui.DockBuilderAddNodeV(dockSpaceId, imgui.DockNodeFlagsDockSpace)

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
	l.wrapNodeV(id, nodeId, content, wrapCfg{})
}

const defaultWindowFlags = imgui.WindowFlagsNone

type wrapCfg struct {
	noWindow  bool
	noPadding bool
}

func (l *Layout) wrapNodeV(id string, nodeId int, content func(), cfg wrapCfg) {
	// Just show the content.
	if cfg.noWindow {
		content()
		return
	}

	if l.app.IsLayoutReset() {
		imgui.DockBuilderDockWindow(id, nodeId)
	}

	if cfg.noPadding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}

	if slice.StrContains(l.tmpNextShowNode, id) {
		imgui.SetNextWindowCollapsed(false, imgui.ConditionOnce)
	}
	if id == l.tmpNextFocusNode {
		imgui.SetNextWindowFocus()
	}

	if imgui.BeginV(id, nil, defaultWindowFlags) {
		if cfg.noPadding {
			imgui.PopStyleVar()
		}
		if imgui.IsWindowDocked() {
			l.tweakWindowNode()
		}
		content()
	} else if cfg.noPadding {
		imgui.PopStyleVar()
	}
	imgui.End()
}

// Tweak window node flags and nodes ordering.
func (l *Layout) tweakWindowNode() {
	if dockNode := imgui.DockBuilderGetNode(imgui.GetWindowDockID()); dockNode != 0 {
		if !l.initialized {
			dockNode.ExtSelectFirstTab() // Ensure that after the launch windows will be selected properly
		}
	}
}
