package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap"
	"github.com/SpaiR/strongdmm/internal/app/ui/component/workspace/widget"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type MapAction interface {
	widget.CanvasAction
}

type Map struct {
	base

	Dmm *dmmap.Dmm

	canvas *widget.Canvas
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{
		Dmm:    dmm,
		canvas: widget.NewCanvas(action, dmm),
	}
	ws.Workspace = ws
	return ws
}

func (m *Map) Name() string {
	return fmt.Sprint(m.Dmm.Name, "##workspace_map_", m.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	size := imgui.WindowSize()
	m.canvas.Process(size.X, size.Y)

	cursor := imgui.CursorPos()
	imgui.SetCursorPos(imgui.Vec2{})
	imgui.Dummy(size)
	imgui.SetCursorPos(cursor)

	minPos := imgui.ItemRectMin()
	maxPos := imgui.ItemRectMax()
	texture := imgui.TextureID(m.canvas.Texture)
	uvMin := imgui.Vec2{X: 0, Y: 1}
	uvMax := imgui.Vec2{X: 1, Y: 0}

	imgui.WindowDrawList().AddImageV(texture, minPos, maxPos, uvMin, uvMax, imguiext.ColorWhitePacked)
}

func (m *Map) Tooltip() string {
	return m.Dmm.Path.Readable
}

func (m *Map) Dispose() {
	m.canvas.Dispose()
	log.Println("[workspace] map workspace disposed:", m.Name())
}
