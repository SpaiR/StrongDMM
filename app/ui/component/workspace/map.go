package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	canvas2 "github.com/SpaiR/strongdmm/app/ui/component/workspace/canvas"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
	"github.com/SpaiR/strongdmm/pkg/imguiext/layout"
)

type MapAction interface {
	canvas2.Action

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)
}

type Map struct {
	base
	action MapAction

	Dmm *dmmap.Dmm

	canvasSate    *canvas2.State
	canvasTools   *canvas2.Tools
	canvasStatus  *canvas2.Status
	canvasControl *canvas2.Control
	canvas        *canvas2.Canvas

	bp *layout.BorderPane

	mouseChangeCbId int
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{Dmm: dmm}

	ws.Workspace = ws
	ws.action = action

	ws.canvasSate = canvas2.NewState(dmm.MaxX, dmm.MaxY, 32) // TODO: world.icon_size
	ws.canvasTools = canvas2.NewTools()
	ws.canvas = canvas2.New(action)
	ws.canvasControl = canvas2.NewControl(ws.canvas.Render.Camera)
	ws.canvasStatus = canvas2.NewStatus(ws.canvasSate)

	ws.bp = layout.NewBorderPane(ws.createLayout())
	ws.mouseChangeCbId = action.AddMouseChangeCallback(ws.mouseChangeCallback)

	ws.canvas.Render.SetOverlayState(ws.canvasSate)
	ws.canvas.Render.UpdateBucket(dmm)

	return ws
}

func (m *Map) Name() string {
	return fmt.Sprint(m.Dmm.Name, "##workspace_map_", m.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	m.bp.Draw()
}

func (m *Map) Tooltip() string {
	return m.Dmm.Path.Readable
}

func (m *Map) Dispose() {
	m.canvas.Dispose()
	m.action.RemoveMouseChangeCallback(m.mouseChangeCbId)
	log.Println("[workspace] map workspace disposed:", m.Name())
}

func (m *Map) Border() bool {
	return false
}

func (m *Map) createLayout() layout.BorderPaneLayout {
	return layout.BorderPaneLayout{
		Top: layout.BorderPaneAreaLayout{Content: m.canvasTools.Process},
		Center: layout.BorderPaneAreaLayout{
			Content:        m.showCanvas,
			PaddingDisable: true,
		},
		Bottom: layout.BorderPaneAreaLayout{Content: m.canvasStatus.Process},
	}
}

func (m *Map) showCanvas() {
	size := imgui.WindowSize()

	m.canvasControl.Process(size)
	m.canvas.Process(size)

	texture := imgui.TextureID(m.canvas.Texture)
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(texture, m.canvasControl.PosMin, m.canvasControl.PosMax, uvMin, uvMax, imguiext.ColorWhitePacked)
}

func (m *Map) mouseChangeCallback(x, y uint) {
	m.updateMousePosition(int(x), int(y))
}

func (m *Map) updateMousePosition(mouseX, mouseY int) {
	// Mouse position relative to canvas.
	relMouseX := mouseX - int(m.canvasControl.PosMin.X)
	relMouseY := mouseY - int(m.canvasControl.PosMin.Y)

	// Canvas height itself.
	canvasHeight := int(m.canvasControl.PosMax.Y - m.canvasControl.PosMin.Y)

	// Mouse position by Y axis, but with bottom-up orientation.
	relMouseY = canvasHeight - relMouseY

	// Transformed coordinates with respect of camera scale and shift.
	relLocalX := float32(relMouseX)/m.canvasControl.Camera.Scale - (m.canvasControl.Camera.ShiftX)
	relLocalY := float32(relMouseY)/m.canvasControl.Camera.Scale - (m.canvasControl.Camera.ShiftY)

	m.canvasSate.SetHoveredTile(relLocalX, relLocalY)
}
