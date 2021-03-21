package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/ui/component/workspace/widget"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type MapAction interface {
	widget.CanvasAction
}

type Map struct {
	base

	Dmm *dmmap.Dmm

	canvas        *widget.Canvas
	canvasControl *widget.CanvasControl
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{Dmm: dmm}
	ws.canvas = widget.NewCanvas(action, dmm)
	ws.canvasControl = widget.NewCanvasControl(ws.canvas.RenderState())
	ws.Workspace = ws
	return ws
}

func (m *Map) Name() string {
	return fmt.Sprint(m.Dmm.Name, "##workspace_map_", m.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	m.showCanvas()
}

func (m *Map) Tooltip() string {
	return m.Dmm.Path.Readable
}

func (m *Map) Dispose() {
	m.canvas.Dispose()
	log.Println("[workspace] map workspace disposed:", m.Name())
}

func (m *Map) showCanvas() {
	size := imgui.WindowSize()
	pos := imgui.WindowPos()

	m.canvasControl.Process(size, pos)
	m.canvas.Process(size)

	texture := imgui.TextureID(m.canvas.Texture)
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(texture, m.canvasControl.PosMin, m.canvasControl.PosMax, uvMin, uvMax, imguiext.ColorWhitePacked)
}
