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
	laterJobs []func()
}

type Config struct {
	IniFilename string
}

func New(config Config) *Window {
	log.Println("[window] creating native window")
	log.Println("[window] config:", config)

	w := Window{
		PointSize: 1.0,
	}

	log.Println("[window] setting up glfw")
	w.setupGlfw()

	log.Println("[window] setting up Dear ImGui")
	w.setupImGui(config)

	log.Println("[window] initializing platform")
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

func (w *Window) RunLater(job func()) {
	w.laterJobs = append(w.laterJobs, job)
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

	window.MakeContextCurrent()
	glfw.SwapInterval(glfw.True)
	window.Show()

	if err := gl.Init(); err != nil {
		log.Fatal("[window] unable to initialize opengl:", err)
	}

	log.Println("[window] opengl initialized")

	w.Handle = window
}

func (w *Window) setupImGui(config Config) {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename(config.IniFilename)
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	// TODO: Fonts configuration
}

func (w *Window) startFrame() {
	gl.Clear(gl.COLOR_BUFFER_BIT)
	platform.NewImGuiGLFWFrame()
	imgui.NewFrame()
	w.runLaterJobs()
}

func (w *Window) runLaterJobs() {
	for _, job := range w.laterJobs {
		job()
	}
	w.laterJobs = nil
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
