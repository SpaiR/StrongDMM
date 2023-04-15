package window

import (
	"image"
	"runtime"

	"sdmm/internal/rsc"

	"sdmm/internal/platform"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/glfw/v3.3/glfw"
	"github.com/rs/zerolog/log"
)

var (
	// AppLogoTexture is a texture pointer to the application logo.
	AppLogoTexture uint32
)

type application interface {
	Process()
	PostProcess()
	CloseCheck()
	IsClosed() bool
	LayoutIniPath() string
}

var (
	pointSize float32 = 1
)

type Window struct {
	handle *glfw.Window

	application application

	mouseChangeCallbackId int
	mouseChangeCallbacks  map[int]func(uint, uint)
}

func (w *Window) Handle() *glfw.Window {
	return w.handle
}

func New(application application) *Window {
	log.Print("creating native window")

	w := Window{application: application}
	w.mouseChangeCallbacks = make(map[int]func(uint, uint))

	log.Print("setting up glfw")
	w.setupGlfw()

	log.Print("setting up Dear ImGui")
	w.setupImGui()

	log.Print("initializing platform")
	platform.InitImGuiGLFW()
	platform.InitImGuiGL()
	platform.MouseChangeCallback = w.mouseChangeCallback

	AppLogoTexture = platform.CreateTexture(rsc.EditorIcon().RGBA())

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

func PointSize() float32 {
	return pointSize
}

func PointSizePtr() *float32 {
	return &pointSize
}

func SetPointSize(ps float32) {
	pointSize = ps
	configureFonts()
	platform.UpdateFontsTexture()
}

func SetFps(value int) {
	log.Print("set fps:", value)
	ticker = newTicker(value)
}

func (w *Window) setupGlfw() {
	runtime.LockOSThread()

	if err := glfw.Init(); err != nil {
		log.Fatal().Msgf("unable to initialize gfw: %v", err)
	}

	glfw.WindowHint(glfw.Visible, glfw.False)

	glfw.WindowHint(glfw.ContextVersionMajor, 3)
	glfw.WindowHint(glfw.ContextVersionMinor, 3)
	glfw.WindowHint(glfw.OpenGLProfile, glfw.OpenGLCoreProfile)
	glfw.WindowHint(glfw.OpenGLForwardCompatible, glfw.True)
	glfw.WindowHint(glfw.Maximized, glfw.True)

	log.Print("glfw initialized")
	log.Print("using opengl 3.3, core profile")

	window, err := glfw.CreateWindow(1280, 768, "", nil, nil)

	if err != nil {
		log.Fatal().Msgf("unable to create window: %v", err)
	}

	log.Print("native window created")

	window.SetIcon([]image.Image{rsc.EditorIcon().RGBA()})
	window.MakeContextCurrent()

	glfw.SwapInterval(glfw.True)

	if err := gl.Init(); err != nil {
		log.Fatal().Msgf("unable to initialize opengl: %v", err)
	}

	window.SetSizeCallback(w.resizeCallback)

	// Ensure that the window is fully initialized before showing.
	RunLater(func() {
		window.Maximize()
		window.Show()
		window.RequestAttention()
	})

	log.Print("opengl initialized")

	w.handle = window
}

func (w *Window) setupImGui() {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename(w.application.LayoutIniPath())
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	w.setDefaultTheme()
}

func (*Window) disposeImGui() {
	if c, err := imgui.CurrentContext(); err == nil {
		c.Destroy()
	}
}

func (w *Window) disposeGlfw() {
	w.handle.Destroy()
	glfw.Terminate()
}

func (w *Window) resizeCallback(_ *glfw.Window, _, _ int) {
	w.runFrame()
}
