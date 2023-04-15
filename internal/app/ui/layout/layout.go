package layout

import (
	"sdmm/internal/app/config"
	"sdmm/internal/app/ui/cpenvironment"
	"sdmm/internal/app/ui/cpprefabs"
	"sdmm/internal/app/ui/cpsearch"
	"sdmm/internal/app/ui/cpvareditor"
	"sdmm/internal/app/ui/cpwsarea"
	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/util/slice"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
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

type layoutNode interface {
	PreProcess()
	Process(dockId int32)
	PostProcess()

	Visible() bool
	SetVisible(bool)

	Focused() bool
	SetFocused(bool)
}

type Layout struct {
	app app

	initialized bool

	leftNodeId      int32
	leftUpNodeId    int32
	leftDownNodeId  int32
	centerNodeId    int32
	rightNodeId     int32
	rightUpNodeId   int32
	rightDownNodeId int32

	Environment *cpenvironment.Environment
	Prefabs     *cpprefabs.Prefabs
	Search      *cpsearch.Search
	WsArea      *cpwsarea.WsArea
	VarEditor   *cpvareditor.VarEditor

	tmpNextShowNode  []string
	tmpNextFocusNode string
}

func New(app app) *Layout {
	l := &Layout{app: app}
	l.loadConfig()

	l.Environment = new(cpenvironment.Environment)
	l.Prefabs = new(cpprefabs.Prefabs)
	l.Search = new(cpsearch.Search)
	l.WsArea = new(cpwsarea.WsArea)
	l.VarEditor = new(cpvareditor.VarEditor)

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
	log.Print("layout state updated:", configState)
}

func (l *Layout) ShowNode(nodeName string) {
	imgui.ExtSetDockTabSelected(nodeName)
	l.tmpNextShowNode = append(l.tmpNextShowNode, nodeName)
}

func (l *Layout) FocusNode(nodeName string) {
	l.tmpNextFocusNode = nodeName
}

func (l *Layout) showEnvironmentNode() {
	l.wrapNode(lnode.NameEnvironment, l.leftNodeId, l.Environment)
}

func (l *Layout) showWorkspaceAreaNode() {
	l.wrapNodeV(lnode.NameWorkspaceArea, l.centerNodeId, l.WsArea, wrapCfg{
		noWindow: true,
	})
}

func (l *Layout) showPrefabsNode() {
	l.wrapNode(lnode.NamePrefabs, l.rightUpNodeId, l.Prefabs)
}

func (l *Layout) showSearchNode() {
	l.wrapNode(lnode.NameSearch, l.rightUpNodeId, l.Search)
}

func (l *Layout) showVariablesNode() {
	l.wrapNode(lnode.NameVariables, l.rightDownNodeId, l.VarEditor)
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

func (l *Layout) wrapNode(id string, dockId int32, node layoutNode) {
	l.wrapNodeV(id, dockId, node, wrapCfg{})
}

const defaultWindowFlags = imgui.WindowFlagsNone

type wrapCfg struct {
	noWindow  bool
	noPadding bool
}

func (l *Layout) wrapNodeV(id string, dockId int32, node layoutNode, cfg wrapCfg) {
	// Just show the content.
	if cfg.noWindow {
		node.Process(dockId)
		return
	}

	l.prepareNode(id, dockId, cfg)
	l.processNode(id, dockId, node, cfg)
}

func (l *Layout) prepareNode(id string, dockId int32, cfg wrapCfg) {
	if l.app.IsLayoutReset() {
		imgui.DockBuilderDockWindow(id, int(dockId))
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
}

func (l *Layout) processNode(id string, dockId int32, node layoutNode, cfg wrapCfg) {
	node.PreProcess()

	visible := imgui.BeginV(id, nil, defaultWindowFlags)

	node.SetVisible(visible)
	node.SetFocused(imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows))

	if visible {
		if cfg.noPadding {
			imgui.PopStyleVar()
		}

		if imgui.IsWindowDocked() {
			l.tweakWindowNode()
		}

		node.Process(dockId)
	} else if cfg.noPadding {
		imgui.PopStyleVar()
	}
	imgui.End()

	node.PostProcess()
}

// Tweak window node flags and nodes ordering.
func (l *Layout) tweakWindowNode() {
	if dockNode := imgui.DockBuilderGetNode(imgui.GetWindowDockID()); dockNode != 0 {
		if !l.initialized {
			dockNode.ExtSelectFirstTab() // Ensure that after the launch windows will be selected properly
		}
	}
}
