package window

import (
	"log"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/platform"
)

const fps int = 60

type Window struct {
	Handle *glfw.Window

	PointSize float32
}

type Config struct {
	IniFilename string
}

func New(config Config) *Window {
	w := Window{
		PointSize: 1.0,
	}

	w.setupGlfw()
	w.setupImGui(config)

	platform.InitImGuiGLFW()
	platform.InitImGuiGL()

	return &w
}

func (w *Window) Run(loop func()) {
	ticker := time.NewTicker(time.Second / time.Duration(fps))

	for !w.Handle.ShouldClose() {
		w.startFrame()
		loop()
		w.endFrame()
		<-ticker.C
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
		log.Fatal("unable to initialize gfw: ", err)
	}

	glfw.WindowHint(glfw.Visible, glfw.False)

	glfw.WindowHint(glfw.ContextVersionMajor, 3)
	glfw.WindowHint(glfw.ContextVersionMinor, 3)
	glfw.WindowHint(glfw.OpenGLProfile, glfw.OpenGLCoreProfile)
	glfw.WindowHint(glfw.OpenGLForwardCompatible, glfw.True)

	window, err := glfw.CreateWindow(1280, 768, "", nil, nil)

	if err != nil {
		log.Fatal("unable to create window: ", err)
	}

	window.MakeContextCurrent()
	glfw.SwapInterval(glfw.True)
	window.Show()

	if err := gl.Init(); err != nil {
		log.Fatal("unable to initialize opengl: ", err)
	}

	w.Handle = window
}

func (w *Window) setupImGui(config Config) {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename(config.IniFilename)
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	// TODO: Fonts configuration
}

func (*Window) startFrame() {
	gl.Clear(gl.COLOR_BUFFER_BIT)
	platform.NewImGuiGLFWFrame()
	imgui.NewFrame()
}

func (w *Window) endFrame() {
	imgui.Render()
	platform.Render(imgui.RenderedDrawData())
	w.Handle.SwapBuffers()
	glfw.PollEvents()
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
