package pmap

import (
	"github.com/SpaiR/imgui-go"
	"log"
	"sdmm/app/command"
	"sdmm/app/render"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/editor"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/tilemenu"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmclip"
	"sdmm/dmapi/dmmsnap"
	"sdmm/imguiext/style"
)

type App interface {
	tilemenu.App

	LoadedEnvironment() *dmenv.Dme

	DoSelectPrefab(prefab *dmmprefab.Prefab)
	DoEditInstance(*dmminstance.Instance)

	SelectedPrefab() (*dmmprefab.Prefab, bool)
	SelectedInstance() (*dmminstance.Instance, bool)

	HasSelectedPrefab() bool
	HasSelectedInstance() bool

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)

	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard
	PathsFilter() *dm.PathsFilter

	ShowLayout(name string, focus bool)

	SyncPrefabs()
	SyncVarEditor()

	PointSize() float32
}

var (
	MirrorCanvasCamera bool
	ActiveCamera       *render.Camera
)

type PaneMap struct {
	app App

	dmm *dmmap.Dmm

	shortcuts shortcut.Shortcuts

	snapshot *dmmsnap.DmmSnap
	editor   *editor.Editor

	tileMenu *tilemenu.TileMenu

	quickEdit *panelQuickEdit

	canvas        *canvas.Canvas
	canvasState   *canvas.State
	canvasControl *canvas.Control
	canvasOverlay *canvas.Overlay

	// ID is needed to dispose a mouse callback when the pane is closed.
	mouseChangeCbId int

	// Properties for the pane.
	pos, size imgui.Vec2
	focused   bool

	panelRightBottomSize imgui.Vec2
	panelBottomSize      imgui.Vec2

	// The value of the Z-level with which the user is currently working.
	activeLevel int

	tmpIsInTemporalToolMode bool
	tmpLastSelectedToolName string
	tmpPrevSelectedToolName string

	tmpLastHoveredInstance *dmminstance.Instance
}

func (p *PaneMap) Canvas() *canvas.Canvas {
	return p.canvas
}

func (p *PaneMap) CanvasState() *canvas.State {
	return p.canvasState
}

func (p *PaneMap) CanvasControl() *canvas.Control {
	return p.canvasControl
}

func (p *PaneMap) CanvasOverlay() *canvas.Overlay {
	return p.canvasOverlay
}

func (p *PaneMap) Editor() *editor.Editor {
	return p.editor
}

func (p *PaneMap) Dmm() *dmmap.Dmm {
	return p.dmm
}

func (p *PaneMap) Focused() bool {
	return p.focused
}

func (p *PaneMap) ActiveLevel() int {
	return p.activeLevel
}

func (p *PaneMap) Size() imgui.Vec2 {
	return p.size
}

func (p *PaneMap) Snapshot() *dmmsnap.DmmSnap {
	return p.snapshot
}

func (p *PaneMap) SetShortcutsVisible(visible bool) {
	p.shortcuts.SetVisible(visible)
}

func New(app App, dmm *dmmap.Dmm) *PaneMap {
	p := &PaneMap{
		app: app,
		dmm: dmm,
	}

	p.activeLevel = 1 // Every map has at least 1 z-level, so we point to it.

	p.snapshot = dmmsnap.New(dmm)
	p.editor = editor.New(app, p, dmm)

	p.tileMenu = tilemenu.New(app, p.editor)

	p.quickEdit = &panelQuickEdit{app: app, editor: p.editor}

	p.canvas = canvas.New()
	p.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmmap.WorldIconSize)
	p.canvasControl = canvas.NewControl()
	p.canvasOverlay = canvas.NewOverlay()

	p.canvasControl.SetOnRmbClick(p.openTileMenu)

	p.canvas.Render().SetOverlay(p.canvasOverlay)
	p.canvas.Render().SetUnitProcessor(p)
	p.canvas.Render().UpdateBucket(p.dmm, p.activeLevel)

	p.mouseChangeCbId = app.AddMouseChangeCallback(p.mouseChangeCallback)
	p.addShortcuts()

	return p
}

func (p *PaneMap) Process() {
	// Enforce a focus to the current window if the canvas was touched.
	if p.canvasControl.Touched() && !imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows) {
		imgui.SetWindowFocus()
	}

	p.updateShortcutsState()

	// Update properties.
	p.pos = imgui.WindowPos().Plus(imgui.WindowContentRegionMin())
	p.size = imgui.WindowSize()
	p.focused = imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)

	p.canvas.Render().SetActiveLevel(p.dmm, p.activeLevel)

	p.canvasControl.Process(p.size)
	p.canvas.Process(p.size)

	p.processCanvasCamera()
	p.processCanvasOverlay()
	p.processCanvasHoveredInstance()

	p.tileMenu.Process()

	p.showCanvas()
	p.showPanel("canvasTool_"+p.dmm.Name, pPosTop, p.showToolsPanel)
	p.showPanelV(
		"quickEdit_"+p.dmm.Name,
		pPosRightBottom,
		p.app.HasSelectedInstance(),
		p.quickEdit.process,
	)
	p.showPanel("canvasStat_"+p.dmm.Name, pPosBottom, p.showStatusPanel)

	p.processTempToolsMode()
}

func (p *PaneMap) Dispose() {
	p.checkActiveCamera()
	p.canvas.Dispose()
	p.app.RemoveMouseChangeCallback(p.mouseChangeCbId)
	p.tileMenu.Dispose()
	p.shortcuts.Dispose()
	log.Println("[pmap] disposed")
}

func (p *PaneMap) prepareTools() {
	log.Println("[pmap] preparing tools:", p.dmm.Name)
	tools.SetEditor(p.editor)
	tools.SetCanvasState(p.canvasState)
	tools.SetCanvasControl(p.canvasControl)
}

func (p *PaneMap) showCanvas() {
	texture := imgui.TextureID(p.canvas.Texture())
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(
		texture,
		p.canvasControl.PosMin(), p.canvasControl.PosMax(),
		uvMin, uvMax,
		style.ColorWhitePacked,
	)
}

func (p *PaneMap) mouseChangeCallback(x, y uint) {
	p.updateCanvasMousePosition(int(x), int(y))
	tools.OnMouseMove()
}

func (p *PaneMap) openTileMenu() {
	if !p.canvasState.HoverOutOfBounds() {
		log.Println("[pmap] open tile menu:", p.canvasState.HoveredTile())
		p.tileMenu.Open(p.canvasState.HoveredTile())
	}
}

func (p *PaneMap) processCanvasHoveredInstance() {
	p.canvasState.SetHoveredInstance(p.tmpLastHoveredInstance)
	p.tmpLastHoveredInstance = nil
}

func (p *PaneMap) updateShortcutsState() {
	if imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows) {
		p.shortcuts.SetVisible(true)
	}
}

func (p *PaneMap) OnActivate() {
	log.Println("[pmap] pane activated:", p.dmm.Name)
	ActiveCamera = p.canvas.Render().Camera()
	p.prepareTools()
	p.focused = true
}

func (p *PaneMap) OnDeactivate() {
	p.focused = false
	tools.Selected().OnDeselect()
	p.checkActiveCamera()
	log.Println("[pmap] pane deactivated:", p.dmm.Name)
}

func (p *PaneMap) checkActiveCamera() {
	if ActiveCamera == p.canvas.Render().Camera() {
		ActiveCamera = nil
		log.Println("[pmap] active camera cleared:", p.dmm.Name)
	}
}
