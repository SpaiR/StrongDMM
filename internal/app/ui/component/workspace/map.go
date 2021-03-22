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
	PointSize() float32
}

type Map struct {
	base
	action MapAction

	Dmm *dmmap.Dmm

	canvasTools   *widget.CanvasTools
	canvas        *widget.Canvas
	canvasControl *widget.CanvasControl
	canvasStatus  *widget.CanvasStatus

	canvasBarHeight float32
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{Dmm: dmm}
	ws.action = action
	ws.canvasTools = widget.NewCanvasTools()
	ws.canvas = widget.NewCanvas(action, dmm)
	ws.canvasControl = widget.NewCanvasControl(ws.canvas.RenderState())
	ws.canvasStatus = widget.NewCanvasStatus()
	ws.Workspace = ws
	return ws
}

func (m *Map) Name() string {
	return fmt.Sprint(m.Dmm.Name, "##workspace_map_", m.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	size := imgui.WindowSize()
	pos := imgui.WindowPos()

	m.showCanvasToolBar(m.calcCanvasToolBarSize(size), m.calcCanvasToolBarPos(pos))
	m.showCanvas(m.calcCanvasSize(size), m.calcCanvasPos(pos))
	m.showCanvasStatusBar(m.calcCanvasStatusBarSize(size), m.calcCanvasStatusBarPos(size))
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

func (m *Map) showCanvasToolBar(size, pos imgui.Vec2) {
	wrapBar("Tool Bar", pos, size, func() {
		m.canvasTools.Process()

		// Tools bar is always at the top and its content is auto-adjusted.
		// Thus we can use it as a ref size.
		m.canvasBarHeight = imgui.WindowHeight()
	})
}

func (m *Map) showCanvas(size, pos imgui.Vec2) {
	m.canvasControl.Process(size, pos)
	m.canvas.Process(size)

	texture := imgui.TextureID(m.canvas.Texture)
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(texture, m.canvasControl.PosMin, m.canvasControl.PosMax, uvMin, uvMax, imguiext.ColorWhitePacked)
}

func (m *Map) showCanvasStatusBar(size, pos imgui.Vec2) {
	wrapBar("Status Bar", pos, size, m.canvasStatus.Process)
}

func wrapBar(name string, pos, size imgui.Vec2, content func()) {
	imgui.SetNextWindowPos(pos)
	imgui.SetNextWindowSize(size)
	imgui.BeginV(name, nil, imgui.WindowFlagsNoDecoration|imgui.WindowFlagsNoMove)
	content()
	imgui.End()
}

func (m *Map) calcCanvasToolBarSize(size imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: size.X, Y: 0}
}

func (m *Map) calcCanvasToolBarPos(pos imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: pos.X, Y: pos.Y - imgui.CurrentStyle().ItemSpacing().Y}
}

func (m *Map) calcCanvasSize(size imgui.Vec2) imgui.Vec2 {
	return size.Minus(imgui.Vec2{Y: m.canvasBarHeight - imgui.CurrentStyle().ItemSpacing().Y})
}

func (m *Map) calcCanvasPos(pos imgui.Vec2) imgui.Vec2 {
	return pos.Plus(imgui.Vec2{Y: m.canvasBarHeight})
}

func (m *Map) calcCanvasStatusBarSize(size imgui.Vec2) imgui.Vec2 {
	return imgui.Vec2{X: size.X, Y: 0}
}

func (m *Map) calcCanvasStatusBarPos(size imgui.Vec2) imgui.Vec2 {
	return m.canvasControl.PosMax.Minus(imgui.Vec2{X: size.X})
}
