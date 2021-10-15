package pmap

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas/tools"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tilemenu"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/snapshot"
	"sdmm/imguiext"
)

type App interface {
	canvas.App
	tilemenu.App

	SelectedInstance() (*dmmdata.Instance, bool)
	HasSelectedInstance() bool

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)

	CommandStorage() *command.Storage
	Clipboard() *dmmap.Clipboard
}

type PaneMap struct {
	app App

	dmm      *dmmap.Dmm
	snapshot *snapshot.Snapshot

	// The value of the Z-level with which the user is currently working.
	activeLevel int

	canvasState   *canvas.State
	canvasStatus  *canvas.Status
	canvasControl *canvas.Control
	canvasTools   *tools.Tools
	canvas        *canvas.Canvas
	tileMenu      *tilemenu.TileMenu

	mouseChangeCbId int

	panePos, paneSize imgui.Vec2
}

func (p *PaneMap) Dmm() *dmmap.Dmm {
	return p.dmm
}

func New(app App, dmm *dmmap.Dmm) *PaneMap {
	p := &PaneMap{
		dmm:         dmm,
		snapshot:    snapshot.NewSnapshot(dmm),
		activeLevel: 1, // Every map has at least 1 z-level, so we point to it.
	}

	p.app = app

	p.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmm.WorldIconSize)
	p.canvas = canvas.New(app)
	p.canvasControl = canvas.NewControl(p.canvas.Render.Camera)
	p.canvasTools = tools.NewTools(p, p.canvasControl, p.canvasState)
	p.canvasStatus = canvas.NewStatus(p.canvasState)
	p.tileMenu = tilemenu.New(app, p)

	p.mouseChangeCbId = app.AddMouseChangeCallback(p.mouseChangeCallback)
	p.canvasControl.SetOnRmbClick(func() { p.tileMenu.Open(p.canvasState.HoveredTile()) })

	p.canvas.Render.SetOverlayState(p.canvasState)
	p.canvas.Render.ValidateLevel(p.dmm, p.activeLevel)

	return p
}

func (p *PaneMap) Process() {
	p.panePos, p.paneSize = imgui.WindowPos(), imgui.WindowSize()
	p.showPanel("canvasTools", pPosTop, p.canvasTools.Process)
	p.showCanvas()
	p.tileMenu.Process()
	p.showPanel("canvasStatus", pPosBottom, p.canvasStatus.Process)
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

func (p *PaneMap) updateMousePosition(mouseX, mouseY int) {
	// If canvas itself is not active, then no need to search for mouse position at all.
	if !p.canvasControl.Active() {
		p.canvasState.SetHoveredTile(-1, -1, -1)
		return
	}

	// Mouse position relative to canvas.
	relMouseX := mouseX - int(p.canvasControl.PosMin.X)
	relMouseY := mouseY - int(p.canvasControl.PosMin.Y)

	// Canvas height itself.
	canvasHeight := int(p.canvasControl.PosMax.Y - p.canvasControl.PosMin.Y)

	// Mouse position by Y axis, but with bottom-up orientation.
	relMouseY = canvasHeight - relMouseY

	// Transformed coordinates with respect of camera scale and shift.
	relLocalX := float32(relMouseX)/p.canvasControl.Camera.Scale - (p.canvasControl.Camera.ShiftX)
	relLocalY := float32(relMouseY)/p.canvasControl.Camera.Scale - (p.canvasControl.Camera.ShiftY)

	p.canvasState.SetHoveredTile(int(relLocalX), int(relLocalY), p.activeLevel)
}
