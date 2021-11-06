package pmap

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tilemenu"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmsnap"
	"sdmm/imguiext"
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
	Clipboard() *dmmap.Clipboard
	PathsFilter() *dm.PathsFilter
}

type PaneMap struct {
	app App

	dmm *dmmap.Dmm

	snapshot *dmmsnap.DmmSnap
	editor   *Editor

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

	// The value of the Z-level with which the user is currently working.
	activeLevel int

	tmpLastHoveredInstance *dmminstance.Instance
}

func (p *PaneMap) Editor() *Editor {
	return p.editor
}

func (p *PaneMap) Dmm() *dmmap.Dmm {
	return p.dmm
}

func New(app App, dmm *dmmap.Dmm) *PaneMap {
	p := &PaneMap{
		app: app,
		dmm: dmm,
	}

	p.activeLevel = 1 // Every map has at least 1 z-level, so we point to it.

	p.snapshot = dmmsnap.New(dmm)
	p.editor = &Editor{pMap: p}

	p.tileMenu = tilemenu.New(app, p.editor)
	p.tools = tools.New(p.editor)

	p.canvas = canvas.New()
	p.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmmap.WorldIconSize)
	p.canvasControl = canvas.NewControl()
	p.canvasOverlay = canvas.NewOverlay()

	p.canvasControl.SetOnLmbClick(p.selectHoveredInstance)
	p.canvasControl.SetOnRmbClick(p.openTileMenu)

	p.canvas.Render().SetOverlay(p.canvasOverlay)
	p.canvas.Render().SetUnitProcessor(p)
	p.canvas.Render().UpdateBucket(p.dmm, p.activeLevel)

	p.tools.SetCanvasState(p.canvasState)
	p.tools.SetCanvasControl(p.canvasControl)

	p.mouseChangeCbId = app.AddMouseChangeCallback(p.mouseChangeCallback)

	return p
}

func (p *PaneMap) Process() {
	// Enforce a focus to the current window if the canvas was touched.
	if p.canvasControl.Touched() && !imgui.IsWindowFocused() {
		imgui.SetWindowFocus()
	}

	p.pos, p.size = imgui.WindowPos(), imgui.WindowSize() // Update properties.
	p.canvas.Render().Camera().Level = p.activeLevel      // Update the canvas camera visible level.

	p.canvasControl.Process(p.size)
	p.canvas.Process(p.size)

	p.processCanvasCamera()
	p.processCanvasOverlay()
	p.processCanvasHoveredInstance()

	p.tools.Process()
	p.tileMenu.Process()

	p.showCanvas()
	p.showPanel("canvasTools", pPosTop, p.showToolsPanel)
	p.showPanel("canvasStatus", pPosBottom, p.showStatusPanel)
}

func (p *PaneMap) Dispose() {
	p.canvas.Dispose()
	p.app.RemoveMouseChangeCallback(p.mouseChangeCbId)
	p.tileMenu.Dispose()
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
		imguiext.ColorWhitePacked,
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
