package canvas

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
)

type Control struct {
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

func (c *Control) Active() bool {
	return c.active
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
	return c.moving || (c.active && c.dragging) || c.clicked
}

func NewControl() *Control {
	return &Control{}
}

func (c *Control) Process(size imgui.Vec2) {
	c.showControlArea(size)

	c.processMouseMove()
	c.processMouseDrag()
	c.processMouseScroll()
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

func (c *Control) processMouseScroll() {
	if !c.active {
		return
	}

	_, mouseWheel := imgui.CurrentIO().MouseWheel()
	c.zoomed = mouseWheel != 0
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
