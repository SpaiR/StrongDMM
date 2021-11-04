package canvas

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/render"
)

const scaleFactor float32 = 1.5

type Control struct {
	camera *render.Camera

	posMin imgui.Vec2
	posMax imgui.Vec2

	active    bool
	activated bool
	moving    bool
	dragging  bool
	zoomed    bool
	clicked   bool

	onLmbClick func()
	onRmbClick func()
}

func (c *Control) Camera() *render.Camera {
	return c.camera
}

func (c *Control) PosMin() imgui.Vec2 {
	return c.posMin
}

func (c *Control) PosMax() imgui.Vec2 {
	return c.posMax
}

func (c *Control) SetOnLmbClick(cb func()) {
	c.onLmbClick = cb
}

func (c *Control) SetOnRmbClick(cb func()) {
	c.onRmbClick = cb
}

func (c *Control) Activated() bool {
	return c.activated
}

func (c *Control) Moving() bool {
	return c.moving
}

func (c *Control) Dragging() bool {
	return c.dragging
}

func (c *Control) Zoomed() bool {
	return c.zoomed
}

func (c *Control) Clicked() bool {
	return c.clicked
}

func (c *Control) Touched() bool {
	return c.moving || (c.active && c.dragging) || c.zoomed || c.clicked
}

func (c *Control) SelectionMode() bool {
	return c.active && (imgui.IsKeyDown(int(glfw.KeyLeftShift)) || imgui.IsKeyDown(int(glfw.KeyRightShift)))
}

func (c *Control) Active() bool {
	return c.active
}

func NewControl(camera *render.Camera) *Control {
	return &Control{
		camera: camera,
	}
}

func (c *Control) Process(size imgui.Vec2, activeLevel int) {
	// Update currently visible level for camera.
	c.camera.Level = activeLevel

	c.showControlArea(size)
	c.processMouseMove()
	c.processMouseDrag()
	c.processMouseScroll(size)
	c.processMouseClick()
}

func (c *Control) showControlArea(size imgui.Vec2) {
	cursor := imgui.CursorPos()
	imgui.SetCursorPos(imgui.Vec2{})
	imgui.Dummy(size)
	imgui.SetCursorPos(cursor)

	active := imgui.IsItemHovered()
	c.activated = active && !c.active
	c.active = active

	c.posMin = imgui.ItemRectMin()
	c.posMax = imgui.ItemRectMax()
}

func (c *Control) processMouseMove() {
	if !c.active && !c.moving || c.activated {
		return
	}

	c.moving = imgui.IsMouseDown(imgui.MouseButtonMiddle) || imgui.IsKeyDown(int(glfw.KeySpace))

	if c.moving {
		if delta := imgui.CurrentIO().MouseDelta(); delta.X != 0 || delta.Y != 0 {
			c.camera.Translate(delta.X/c.camera.Scale, -delta.Y/c.camera.Scale)
		}
	}
}

func (c *Control) processMouseDrag() {
	if c.moving {
		return
	}

	isLmbDown := imgui.IsMouseDown(imgui.MouseButtonLeft)
	if isLmbDown && !c.dragging {
		c.dragging = true
	} else if !isLmbDown && c.dragging {
		c.dragging = false
	}
}

func (c *Control) processMouseScroll(size imgui.Vec2) {
	if !c.active {
		return
	}

	_, mouseWheel := imgui.CurrentIO().MouseWheel()
	c.zoomed = mouseWheel != 0

	if !c.zoomed {
		return
	}

	zoomIn := mouseWheel > 0
	scale := c.camera.Scale

	if zoomIn {
		scale *= -scaleFactor
	}

	mousePos := imgui.MousePos()
	localPos := mousePos.Minus(c.posMin)

	offsetX := localPos.X / scale / 2
	offsetY := (size.Y - localPos.Y) / scale / 2

	c.camera.Translate(offsetX, offsetY)
	c.camera.Zoom(zoomIn, scaleFactor)
}

func (c *Control) processMouseClick() {
	c.clicked = imgui.IsMouseClicked(imgui.MouseButtonLeft | imgui.MouseButtonMiddle | imgui.MouseButtonRight)
	if c.active {
		if imgui.IsMouseClicked(imgui.MouseButtonLeft) && c.onLmbClick != nil {
			c.onLmbClick()
		}
		if imgui.IsMouseClicked(imgui.MouseButtonRight) && c.onRmbClick != nil {
			c.onRmbClick()
		}
	}
}
