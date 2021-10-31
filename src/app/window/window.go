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

	process, postProcess func()

	mouseChangeCallbackId int
	mouseChangeCallbacks  map[int]func(uint, uint)

	pointSize float32
	laterJobs []func()
}

func New(process func(), postProcess func()) *Window {
	log.Println("[window] creating native window")

	w := Window{process: process, postProcess: postProcess, pointSize: 1}
	w.mouseChangeCallbacks = make(map[int]func(uint, uint))

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

func (w *Window) PointSize() float32 {
	return w.pointSize
}

func (w *Window) SetPointSize(pointSize float32) {
	w.pointSize = pointSize
	w.configureFonts()
	platform.UpdateFontsTexture()
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

	if err := gl.Init(); err != nil {
		log.Fatal("[window] unable to initialize opengl:", err)
	}

	window.Maximize()
	window.SetSizeCallback(w.resizeCallback)

	log.Println("[window] opengl initialized")

	w.Handle = window
}

func (w *Window) setupImGui() {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename("")
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	// TODO: Proper theming
	imgui.StyleColorsDark()
	imgui.CurrentStyle().SetWindowBorderSize(0)
	imgui.CurrentStyle().SetChildBorderSize(0)
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

func (w *Window) resizeCallback(_ *glfw.Window, _, _ int) {
	w.runFrame()
}
