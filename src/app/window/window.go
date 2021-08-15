package window

import (
	"image"
	"log"
	"runtime"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/assets"
	"sdmm/platform"
)

const fps int = 60

type Window struct {
	Handle *glfw.Window

	mouseChangeCallbackId int
	mouseChangeCallbacks  map[int]func(uint, uint)

	PointSize float32
	laterJobs []func()
}

func New() *Window {
	log.Println("[window] creating native window")

	w := Window{
		PointSize:            2.5, // TODO: make configurable and 1 by default
		mouseChangeCallbacks: make(map[int]func(uint, uint)),
	}

	log.Println("[window] setting up glfw")
	w.setupGlfw()

	log.Println("[window] setting up Dear ImGui")
	w.setupImGui()

	log.Println("[window] initializing platform")
	platform.InitImGuiGLFW()
	platform.InitImGuiGL()
	platform.MouseChangeCallback = w.mouseChangeCallback

	return &w
}

func (w *Window) mouseChangeCallback(x, y uint) {
	for _, cb := range w.mouseChangeCallbacks {
		cb(x, y)
	}
}

func (w *Window) Dispose() {
	platform.DisposeImGuiGL()
	platform.DisposeImGuiGLFW()

	w.disposeImGui()
	w.disposeGlfw()
}

func (w *Window) setupGlfw() {
	runtime.LockOSThread()

	if err := glfw.Init(); err != nil {
		log.Fatal("[window] unable to initialize gfw:", err)
	}

	glfw.WindowHint(glfw.Visible, glfw.False)

	glfw.WindowHint(glfw.ContextVersionMajor, 3)
	glfw.WindowHint(glfw.ContextVersionMinor, 3)
	glfw.WindowHint(glfw.OpenGLProfile, glfw.OpenGLCoreProfile)
	glfw.WindowHint(glfw.OpenGLForwardCompatible, glfw.True)

	log.Println("[window] glfw initialized")
	log.Println("[window] using opengl 3.3, core profile")

	window, err := glfw.CreateWindow(1280, 768, "", nil, nil)

	if err != nil {
		log.Fatal("[window] unable to create window:", err)
	}

	log.Println("[window] native window created")

	window.SetIcon([]image.Image{assets.EditorIcon().RGBA()})
	window.MakeContextCurrent()
	glfw.SwapInterval(glfw.True)
	window.Maximize()

	if err := gl.Init(); err != nil {
		log.Fatal("[window] unable to initialize opengl:", err)
	}

	log.Println("[window] opengl initialized")

	w.Handle = window
}

func (w *Window) setupImGui() {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename("")
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	// TODO: Proper theming
	imgui.StyleColorsLight()
	imgui.CurrentStyle().SetWindowBorderSize(0)
	imgui.CurrentStyle().SetChildBorderSize(0)

	w.configureFonts()
}

func (*Window) disposeImGui() {
	if c, err := imgui.CurrentContext(); err == nil {
		c.Destroy()
	}
}

func (w *Window) disposeGlfw() {
	w.Handle.Destroy()
	glfw.Terminate()
}
