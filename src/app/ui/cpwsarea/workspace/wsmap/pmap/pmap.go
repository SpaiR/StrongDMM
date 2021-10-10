package pmap

import (
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/canvas/tools"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/snapshot"
	"sdmm/imguiext"
)

type Action interface {
	canvas.Action

	AppSelectedInstance() (*dmmdata.Instance, bool)
	AppHasSelectedInstance() bool

	AppAddMouseChangeCallback(cb func(uint, uint)) int
	AppRemoveMouseChangeCallback(id int)

	AppPushCommand(command command.Command)
}

type PaneMap struct {
	action Action

	dmm      *dmmap.Dmm
	snapshot *snapshot.Snapshot

	// The value of the Z-level with which the user is currently working.
	activeLevel int

	canvasState   *canvas.State
	canvasStatus  *canvas.Status
	canvasControl *canvas.Control
	canvasTools   *tools.Tools
	canvas        *canvas.Canvas

	mouseChangeCbId int

	panePos, paneSize imgui.Vec2
}

func (p *PaneMap) Dmm() *dmmap.Dmm {
	return p.dmm
}

func New(action Action, dmm *dmmap.Dmm) *PaneMap {
	ws := &PaneMap{
		dmm:         dmm,
		snapshot:    snapshot.NewSnapshot(dmm),
		activeLevel: 1, // Every map has at least 1 z-level, so we point to it.
	}

	ws.action = action

	ws.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, dmm.WorldIconSize)
	ws.canvas = canvas.New(action)
	ws.canvasControl = canvas.NewControl(ws.canvas.Render.Camera)
	ws.canvasTools = tools.NewTools(ws, ws.canvasControl, ws.canvasState)
	ws.canvasStatus = canvas.NewStatus(ws.canvasState)

	ws.mouseChangeCbId = action.AppAddMouseChangeCallback(ws.mouseChangeCallback)

	ws.canvas.Render.SetOverlayState(ws.canvasState)
	ws.canvas.Render.ValidateLevel(ws.dmm, ws.activeLevel)

	return ws
}

func (p *PaneMap) Process() {
	p.panePos, p.paneSize = imgui.WindowPos(), imgui.WindowSize()
	p.showPanel("canvasTools", pPosTop, p.canvasTools.Process)
	p.showCanvas()
	p.showPanel("canvasStatus", pPosBottom, p.canvasStatus.Process)
}

func (p *PaneMap) Dispose() {
	p.canvas.Dispose()
	p.action.AppRemoveMouseChangeCallback(p.mouseChangeCbId)
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
