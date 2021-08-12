package window

import (
	"log"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/glfw/v3.3/glfw"
	"strongdmm/platform"
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
		PointSize:            1.0,
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

func (w *Window) AddMouseChangeCallback(cb func(uint, uint)) (callbackId int) {
	id := w.mouseChangeCallbackId
	w.mouseChangeCallbacks[id] = cb
	w.mouseChangeCallbackId++
	log.Println("[window] mouse change callback added: ", id)
	return id
}

func (w *Window) RemoveMouseChangeCallback(id int) {
	delete(w.mouseChangeCallbacks, id)
	log.Println("[window] mouse change callback deleted: ", id)

}

func (w *Window) AppRunLater(job func()) {
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

func (w *Window) setupImGui() {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename("")
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)

	// TODO: Proper theming
	imgui.StyleColorsLight()
	imgui.CurrentStyle().SetWindowBorderSize(0)
	imgui.CurrentStyle().SetChildBorderSize(0)

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

func (w *Window) mouseChangeCallback(x, y uint) {
	for _, cb := range w.mouseChangeCallbacks {
		cb(x, y)
	}
}
