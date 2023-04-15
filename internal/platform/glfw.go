package platform

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"github.com/rs/zerolog/log"
)

var (
	c clipboard

	mouseCursors     [imgui.MouseCursorCount]*glfw.Cursor
	mouseJustPressed [3]bool

	deltaTime float64

	MouseChangeCallback func(x, y uint)
)

type clipboard struct {
	w *glfw.Window
}

func (c clipboard) Text() (string, error) {
	return c.w.GetClipboardString(), nil
}

func (c clipboard) SetText(value string) {
	c.w.SetClipboardString(value)
}

func SetClipboard(str string) {
	c.SetText(str)
}

func GetClipboard() string {
	str, _ := c.Text()
	return str
}

func InitImGuiGLFW() {
	window := glfw.GetCurrentContext()
	c = clipboard{w: window}

	io := imgui.CurrentIO()

	io.SetClipboard(c)
	io.SetBackendFlags(imgui.BackendFlagsHasMouseCursors | imgui.BackendFlagsHasSetMousePos)

	log.Print("clipboard initialized")

	io.KeyMap(imgui.KeyTab, int(glfw.KeyTab))
	io.KeyMap(imgui.KeyLeftArrow, int(glfw.KeyLeft))
	io.KeyMap(imgui.KeyRightArrow, int(glfw.KeyRight))
	io.KeyMap(imgui.KeyUpArrow, int(glfw.KeyUp))
	io.KeyMap(imgui.KeyDownArrow, int(glfw.KeyDown))
	io.KeyMap(imgui.KeyPageUp, int(glfw.KeyPageUp))
	io.KeyMap(imgui.KeyPageDown, int(glfw.KeyPageDown))
	io.KeyMap(imgui.KeyHome, int(glfw.KeyHome))
	io.KeyMap(imgui.KeyEnd, int(glfw.KeyEnd))
	io.KeyMap(imgui.KeyInsert, int(glfw.KeyInsert))
	io.KeyMap(imgui.KeyDelete, int(glfw.KeyDelete))
	io.KeyMap(imgui.KeyBackspace, int(glfw.KeyBackspace))
	io.KeyMap(imgui.KeySpace, int(glfw.KeySpace))
	io.KeyMap(imgui.KeyEnter, int(glfw.KeyEnter))
	io.KeyMap(imgui.KeyKeyPadEnter, int(glfw.KeyKPEnter))
	io.KeyMap(imgui.KeyEscape, int(glfw.KeyEscape))
	io.KeyMap(imgui.KeyA, int(glfw.KeyA))
	io.KeyMap(imgui.KeyC, int(glfw.KeyC))
	io.KeyMap(imgui.KeyV, int(glfw.KeyV))
	io.KeyMap(imgui.KeyX, int(glfw.KeyX))
	io.KeyMap(imgui.KeyY, int(glfw.KeyY))
	io.KeyMap(imgui.KeyZ, int(glfw.KeyZ))

	log.Print("key map initialized")

	mouseCursors[imgui.MouseCursorArrow] = glfw.CreateStandardCursor(glfw.ArrowCursor)
	mouseCursors[imgui.MouseCursorTextInput] = glfw.CreateStandardCursor(glfw.IBeamCursor)
	mouseCursors[imgui.MouseCursorResizeAll] = glfw.CreateStandardCursor(glfw.ArrowCursor)
	mouseCursors[imgui.MouseCursorResizeNS] = glfw.CreateStandardCursor(glfw.VResizeCursor)
	mouseCursors[imgui.MouseCursorResizeEW] = glfw.CreateStandardCursor(glfw.HResizeCursor)
	mouseCursors[imgui.MouseCursorResizeNESW] = glfw.CreateStandardCursor(glfw.ArrowCursor)
	mouseCursors[imgui.MouseCursorResizeNWSE] = glfw.CreateStandardCursor(glfw.ArrowCursor)
	mouseCursors[imgui.MouseCursorHand] = glfw.CreateStandardCursor(glfw.HandCursor)
	//mouseCursors[imgui.MouseCursorNotAllowed] = glfw.CreateStandardCursor(glfw.ArrowCursor)

	log.Print("mouse cursors initialized")

	window.SetMouseButtonCallback(mouseButtonCallback)
	window.SetScrollCallback(mouseScrollCallback)
	window.SetKeyCallback(keyCallback)
	window.SetCharCallback(charCallback)
	window.SetCursorPosCallback(cursorPosCallback)

	log.Print("callbacks initialized")
}

func NewImGuiGLFWFrame() {
	io := imgui.CurrentIO()
	window := glfw.GetCurrentContext()

	winWidth, winHeight := window.GetSize()
	fbWidth, fbHeight := window.GetFramebufferSize()

	io.SetDisplaySize(imgui.Vec2{X: float32(winWidth), Y: float32(winHeight)})

	if winWidth > 0 && winHeight > 0 {
		io.SetDisplayFrameBufferScale(imgui.Vec2{X: float32(fbWidth / winWidth), Y: float32(fbHeight / winHeight)})
	}

	updateTime(&io)
	updateMousePosAndButtons()
	updateMouseCursor()
}

func DisposeImGuiGLFW() {
	for idx := range mouseCursors {
		mouseCursors[idx].Destroy()
	}
}

func updateTime(io *imgui.IO) {
	currentTime := glfw.GetTime()
	if deltaTime > 0 {
		io.SetDeltaTime(float32(currentTime - deltaTime))
	} else {
		io.SetDeltaTime(1.0 / 60.0)
	}
	deltaTime = currentTime
}

func updateMousePosAndButtons() {
	io := imgui.CurrentIO()
	window := glfw.GetCurrentContext()

	for i := 0; i < len(mouseJustPressed); i++ {
		io.SetMouseButtonDown(i, mouseJustPressed[i] || (window.GetMouseButton(glfw.MouseButton(i)) == glfw.Press))
		mouseJustPressed[i] = false
	}

	if window.GetAttrib(glfw.Focused) != 0 {
		x, y := window.GetCursorPos()
		io.SetMousePosition(imgui.Vec2{X: float32(x), Y: float32(y)})
	}
}

func updateMouseCursor() {
	window := glfw.GetCurrentContext()

	if window.GetInputMode(glfw.CursorMode) == glfw.CursorDisabled {
		return
	}

	imguiCursor := imgui.MouseCursor()

	if imguiCursor == imgui.MouseCursorNone {
		window.SetInputMode(glfw.CursorMode, glfw.CursorHidden)
	} else {
		window.SetCursor(mouseCursors[imguiCursor])
		window.SetInputMode(glfw.CursorMode, glfw.CursorNormal)
	}
}

func mouseButtonCallback(_ *glfw.Window, button glfw.MouseButton, action glfw.Action, _ glfw.ModifierKey) {
	if action == glfw.Press && button >= 0 && int(button) < len(mouseJustPressed) {
		mouseJustPressed[button] = true
	}
}

func mouseScrollCallback(_ *glfw.Window, x, y float64) {
	imgui.CurrentIO().AddMouseWheelDelta(float32(x), float32(y))
}

func keyCallback(_ *glfw.Window, key glfw.Key, _ int, action glfw.Action, _ glfw.ModifierKey) {
	io := imgui.CurrentIO()

	if action == glfw.Press {
		io.KeyPress(int(key))
	}
	if action == glfw.Release {
		io.KeyRelease(int(key))
	}

	// Modifiers are not reliable across systems
	io.KeyCtrl(int(glfw.KeyLeftControl), int(glfw.KeyRightControl))
	io.KeyShift(int(glfw.KeyLeftShift), int(glfw.KeyRightShift))
	//io.KeyAlt(int(glfw.KeyLeftAlt), int(glfw.KeyRightAlt)) // ImGui has strange widows behaviour when alt pressed.
	io.KeySuper(int(glfw.KeyLeftSuper), int(glfw.KeyRightSuper))
}

func charCallback(_ *glfw.Window, char rune) {
	imgui.CurrentIO().AddInputCharacters(string(char))
}

func cursorPosCallback(_ *glfw.Window, posX, posY float64) {
	if MouseChangeCallback != nil {
		MouseChangeCallback(uint(posX), uint(posY))
	}
}
