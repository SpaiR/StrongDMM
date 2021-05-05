package canvas

import (
	"github.com/SpaiR/imgui-go"
	render2 "github.com/SpaiR/strongdmm/app/render"
	"github.com/go-gl/glfw/v3.3/glfw"
)

const scaleFactor float32 = 1.5

type Control struct {
	Camera *render2.Camera

	PosMin imgui.Vec2
	PosMax imgui.Vec2

	active    bool
	activated bool
	dragging  bool
}

func NewControl(camera *render2.Camera) *Control {
	return &Control{
		Camera: camera,
	}
}

func (c *Control) Process(size imgui.Vec2) {
	c.showControlArea(size)
	c.processMouseMove()
	c.processMouseScroll(size)
}

func (c *Control) showControlArea(size imgui.Vec2) {
	cursor := imgui.CursorPos()
	imgui.SetCursorPos(imgui.Vec2{})
	imgui.Dummy(size)
	imgui.SetCursorPos(cursor)

	active := imgui.IsItemHovered()
	c.activated = active && !c.active
	c.active = active

	c.PosMin = imgui.ItemRectMin()
	c.PosMax = imgui.ItemRectMax()
}

func (c *Control) processMouseMove() {
	if !c.active && !c.dragging || c.activated {
		return
	}

	c.dragging = imgui.IsMouseDown(imgui.MouseButtonMiddle) || imgui.IsKeyDown(int(glfw.KeySpace))

	if c.dragging {
		if delta := imgui.CurrentIO().MouseDelta(); delta.X != 0 || delta.Y != 0 {
			c.Camera.Translate(delta.X/c.Camera.Scale, -delta.Y/c.Camera.Scale)
		}
	}
}

func (c *Control) processMouseScroll(size imgui.Vec2) {
	if !c.active {
		return
	}

	_, mouseWheel := imgui.CurrentIO().MouseWheel()
	if mouseWheel == 0 {
		return
	}

	zoomIn := mouseWheel > 0
	scale := c.Camera.Scale

	if zoomIn {
		scale *= -scaleFactor
	}

	mousePos := imgui.MousePos()
	localPos := mousePos.Minus(c.PosMin)

	offsetX := localPos.X / scale / 2
	offsetY := (size.Y - localPos.Y) / scale / 2

	c.Camera.Translate(offsetX, offsetY)
	c.Camera.Zoom(zoomIn, scaleFactor)
}
