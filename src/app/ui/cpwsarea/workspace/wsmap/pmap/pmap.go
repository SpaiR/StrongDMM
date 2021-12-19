package pmap

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/editor"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tilemenu"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmclip"
	"sdmm/dmapi/dmmsnap"
	"sdmm/imguiext"
	"sdmm/imguiext/style"
)

type App interface {
	tilemenu.App

	DoSelectPrefab(prefab *dmmprefab.Prefab)
	DoEditInstance(*dmminstance.Instance)

	SelectedPrefab() (*dmmprefab.Prefab, bool)
	HasSelectedPrefab() bool

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)

	CommandStorage() *command.Storage
	Clipboard() *dmmclip.Clipboard
	PathsFilter() *dm.PathsFilter

	ShowLayout(name string, focus bool)

	SyncPrefabs()
	SyncVarEditor()
}

type PaneMap struct {
	app App

	dmm *dmmap.Dmm

	shortcuts shortcut.Shortcuts

	snapshot *dmmsnap.DmmSnap
	editor   *editor.Editor

	tools    *tools.Tools
	tileMenu *tilemenu.TileMenu

	canvas        *canvas.Canvas
	canvasState   *canvas.State
	canvasControl *canvas.Control
	canvasOverlay *canvas.Overlay

	// ID is needed to dispose a mouse callback when the pane is closed.
	mouseChangeCbId int

	// Properties for the pane.
	pos, size imgui.Vec2
	focused   bool

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
	p.tools = tools.New(p.editor)

	p.canvas = canvas.New()
	p.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmmap.WorldIconSize)
	p.canvasControl = canvas.NewControl()
	p.canvasOverlay = canvas.NewOverlay()

	p.canvasControl.SetOnRmbClick(p.openTileMenu)

	p.canvas.Render().SetOverlay(p.canvasOverlay)
	p.canvas.Render().SetUnitProcessor(p)
	p.canvas.Render().UpdateBucket(p.dmm, p.activeLevel)

	p.tools.SetCanvasState(p.canvasState)
	p.tools.SetCanvasControl(p.canvasControl)

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
	p.pos, p.size = imgui.WindowPos(), imgui.WindowSize()
	p.focused = imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)

	p.canvas.Render().Camera().Level = p.activeLevel // Update the canvas camera visible level.

	p.canvasControl.Process(p.size)
	p.canvas.Process(p.size)

	p.processCanvasCamera()
	p.processCanvasOverlay()
	p.processCanvasHoveredInstance()

	p.tools.Process(imguiext.IsAltDown()) // Enable tools alt-behaviour when Alt button is down.
	p.tileMenu.Process()

	p.showCanvas()
	p.showPanel("canvasTools", pPosTop, p.showToolsPanel)
	p.showPanel("canvasStatus", pPosBottom, p.showStatusPanel)

	p.processTempToolsMode()
}

func (p *PaneMap) Dispose() {
	p.canvas.Dispose()
	p.app.RemoveMouseChangeCallback(p.mouseChangeCbId)
	p.tileMenu.Dispose()
	p.shortcuts.Dispose()
	log.Println("[pmap] disposed")
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
	p.tools.OnMouseMove()
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
