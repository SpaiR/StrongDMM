package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"github.com/SpaiR/strongdmm/app/command"
	"github.com/SpaiR/strongdmm/app/ui/component/workspace/canvas"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	"github.com/SpaiR/strongdmm/pkg/dm/snapshot"
	"github.com/SpaiR/strongdmm/pkg/util"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
	"github.com/SpaiR/strongdmm/pkg/imguiext/layout"
)

type MapAction interface {
	canvas.Action

	SelectedInstance() *dmminstance.Instance
	HasSelectedInstance() bool

	AddMouseChangeCallback(cb func(uint, uint)) int
	RemoveMouseChangeCallback(id int)

	SetCommandStack(id string)
	PushCommand(command command.Command)
}

type Map struct {
	base
	action MapAction

	Dmm      *dmmap.Dmm
	Snapshot *snapshot.Snapshot

	canvasState   *canvas.State
	canvasStatus  *canvas.Status
	canvasControl *canvas.Control
	canvasTools   *canvas.Tools
	canvas        *canvas.Canvas

	bp *layout.BorderPane

	mouseChangeCbId int
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{
		Dmm:      dmm,
		Snapshot: snapshot.NewSnapshot(dmm),
	}

	ws.Workspace = ws
	ws.action = action

	ws.canvasState = canvas.NewState(dmm.MaxX, dmm.MaxY, 32) // TODO: world.icon_size
	ws.canvas = canvas.New(action)
	ws.canvasControl = canvas.NewControl(ws.canvas.Render.Camera)
	ws.canvasTools = canvas.NewTools(ws, ws.canvasControl, ws.canvasState)
	ws.canvasStatus = canvas.NewStatus(ws.canvasState)

	ws.bp = layout.NewBorderPane(ws.createLayout())
	ws.mouseChangeCbId = action.AddMouseChangeCallback(ws.mouseChangeCallback)

	ws.canvas.Render.SetOverlayState(ws.canvasState)
	ws.canvas.Render.UpdateBucket(ws.Dmm)

	return ws
}

func (m *Map) Name() string {
	return fmt.Sprint(m.Dmm.Name, "##workspace_map_", m.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	m.action.SetCommandStack(m.Name())
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

// AddSelectedInstance adds currently selected instance on the map.
// If there is no selected instance, then nothing will happen.
func (m *Map) AddSelectedInstance(pos util.Point) {
	if instance := m.action.SelectedInstance(); instance != nil {
		tile := m.Dmm.GetTile(pos.X, pos.Y, 1) // TODO: respect Z-level
		tile.Content = append(tile.Content, instance)
		m.canvas.Render.UpdateBucketV(m.Dmm, []util.Point{pos})
	}
}

func (m *Map) HasSelectedInstance() bool {
	return m.action.HasSelectedInstance()
}

// CommitChanges triggers snapshot to commit changes and create a patch between two map states.
func (m *Map) CommitChanges(changesType string) {
	stateId := m.Snapshot.Commit()
	m.action.PushCommand(command.New(changesType, func() {
		m.Snapshot.GoTo(stateId - 1)
		m.canvas.Render.UpdateBucket(m.Dmm)
	}, func() {
		m.Snapshot.GoTo(stateId)
		m.canvas.Render.UpdateBucket(m.Dmm)
	}))
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
	// If canvas itself is not active, then no need to search for mouse position at all.
	if !m.canvasControl.Active() {
		m.canvasState.SetHoveredTile(-1, -1)
		return
	}

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

	m.canvasState.SetHoveredTile(relLocalX, relLocalY)
}
