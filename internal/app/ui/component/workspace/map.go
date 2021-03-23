package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/ui/component/workspace/canvas"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
	"github.com/SpaiR/strongdmm/pkg/imguiext/pane"
)

type MapAction interface {
	canvas.Action
	PointSize() float32
}

type Map struct {
	base
	action MapAction

	Dmm *dmmap.Dmm

	canvasTools   *canvas.Tools
	canvasStatus  *canvas.Status
	canvasControl *canvas.Control
	canvas        *canvas.Canvas

	bp *pane.Border
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{Dmm: dmm}
	ws.action = action
	ws.canvasTools = canvas.NewTools()
	ws.canvas = canvas.New(action, dmm)
	ws.canvasControl = canvas.NewControl(ws.canvas.RenderState())
	ws.canvasStatus = canvas.NewStatus()
	ws.bp = pane.NewBorder(ws.createLayout())
	ws.Workspace = ws
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
	log.Println("[workspace] map workspace disposed:", m.Name())
}

func (m *Map) Border() bool {
	return false
}

func (m *Map) createLayout() pane.BorderLayout {
	return pane.BorderLayout{
		Top:    m.canvasTools.Process,
		Center: m.showCanvas,
		Bottom: m.canvasStatus.Process,

		CenterPaddingDisable: true,
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
