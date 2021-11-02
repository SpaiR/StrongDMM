package pmap

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas/tools"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tilemenu"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/dmapi/dmmsnap"
	"sdmm/imguiext"
)

type App interface {
	canvas.App
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

	dmm      *dmmap.Dmm
	snapshot *dmmsnap.DmmSnap

	canvas        *canvas.Canvas
	canvasState   *canvas.State
	canvasControl *canvas.Control
	canvasTools   *tools.Tools
	canvasStatus  *canvas.Status

	tileMenu *tilemenu.TileMenu

	mouseChangeCbId int

	panePos, paneSize imgui.Vec2

	// The value of the Z-level with which the user is currently working.
	activeLevel int

	tmpLastHoveredInstance *dmminstance.Instance
}

func (p *PaneMap) Dmm() *dmmap.Dmm {
	return p.dmm
}

func (p *PaneMap) SelectInstance(i *dmminstance.Instance) {
	p.app.DoSelectPrefab(i.Prefab())
	p.app.DoEditInstance(i)
}

func New(app App, dmm *dmmap.Dmm) *PaneMap {
	p := &PaneMap{
		app: app,
		dmm: dmm,

		activeLevel: 1, // Every map has at least 1 z-level, so we point to it.
	}

	p.snapshot = dmmsnap.New(dmm)
	p.canvas = canvas.New(app)
	p.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmmap.WorldIconSize)
	p.canvasControl = canvas.NewControl(p.canvas.Render.Camera)
	p.canvasTools = tools.NewTools(p, p.canvasControl, p.canvasState)
	p.canvasStatus = canvas.NewStatus(p.canvasState)
	p.tileMenu = tilemenu.New(app, p)

	p.mouseChangeCbId = app.AddMouseChangeCallback(p.mouseChangeCallback)

	p.canvasControl.SetOnLmbClick(p.selectHoveredInstance)
	p.canvasControl.SetOnRmbClick(p.openTileMenu)

	p.canvas.Render.SetOverlayState(p.canvasState)
	p.canvas.Render.SetUnitProcessor(p)
	p.canvas.Render.ValidateLevel(p.dmm, p.activeLevel)

	return p
}

func (p *PaneMap) Process() {
	if p.canvasControl.Touched() && !imgui.IsWindowFocused() {
		imgui.SetWindowFocus()
	}

	p.panePos, p.paneSize = imgui.WindowPos(), imgui.WindowSize()
	p.showPanel("canvasTools", pPosTop, p.canvasTools.Process)
	p.showCanvas()
	p.tileMenu.Process()
	p.showPanel("canvasStatus", pPosBottom, p.canvasStatus.Process)

	p.updateHoveredInstance()
}

func (p *PaneMap) Dispose() {
	p.canvas.Dispose()
	p.app.RemoveMouseChangeCallback(p.mouseChangeCbId)
	p.tileMenu.Dispose()
	log.Println("[pmap] disposed")
}

func (p *PaneMap) showCanvas() {
	p.canvasControl.Process(p.paneSize, p.activeLevel)
	p.canvas.Process(p.paneSize)

	texture := imgui.TextureID(p.canvas.Texture)
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(
		texture,
		p.canvasControl.PosMin, p.canvasControl.PosMax,
		uvMin, uvMax,
		imguiext.ColorWhitePacked,
	)
}

func (p *PaneMap) mouseChangeCallback(x, y uint) {
	p.updateMousePosition(int(x), int(y))
}

func (p *PaneMap) updateHoveredInstance() {
	if !p.canvasControl.SelectionMode() || p.canvasState.HoverOutOfBounds() {
		p.tmpLastHoveredInstance = nil
	}
	p.canvasState.SetHoveredInstance(p.tmpLastHoveredInstance)
}

func (p *PaneMap) updateMousePosition(mouseX, mouseY int) {
	// If canvas itself is not active, then no need to search for mouse position at all.
	if !p.canvasControl.Active() {
		p.canvasState.SetMousePosition(-1, -1, -1)
		return
	}

	// Mouse position relative to canvas.
	relMouseX := float32(mouseX - int(p.canvasControl.PosMin.X))
	relMouseY := float32(mouseY - int(p.canvasControl.PosMin.Y))

	// Canvas height itself.
	canvasHeight := p.canvasControl.PosMax.Y - p.canvasControl.PosMin.Y

	// Mouse position by Y axis, but with bottom-up orientation.
	relMouseY = canvasHeight - relMouseY

	// Transformed coordinates with respect of camera scale and shift.
	relMouseX = relMouseX/p.canvasControl.Camera.Scale - (p.canvasControl.Camera.ShiftX)
	relMouseY = relMouseY/p.canvasControl.Camera.Scale - (p.canvasControl.Camera.ShiftY)

	p.canvasState.SetMousePosition(int(relMouseX), int(relMouseY), p.activeLevel)
}

func (p *PaneMap) selectHoveredInstance() {
	if hoveredInstance := p.canvasState.HoveredInstance(); hoveredInstance != nil && p.canvasControl.SelectionMode() {
		log.Println("[pmap] selected hovered instance:", hoveredInstance.Id())
		p.SelectInstance(hoveredInstance)
	}
}

func (p *PaneMap) openTileMenu() {
	if !p.canvasState.HoverOutOfBounds() {
		log.Println("[pmap] open tile menu:", p.canvasState.HoveredTile())
		p.tileMenu.Open(p.canvasState.HoveredTile())
	}
}
