package window

import (
	"os"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/gl/v3.3-core/gl"
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/platform"
)

const fps int = 60

type windowApp interface {
	Initialize()
	Loop()
	Dispose()
}

func ShowAndRun(app windowApp) {
	setupGlfw()
	setupImGui()

	platform.InitImGuiGLFW()
	platform.InitImGuiGL()

	app.Initialize()
	loop(app)
	app.Dispose()

	platform.DisposeImGuiGL()
	platform.DisposeImGuiGLFW()

	disposeImGui()
	disposeGlfw()

	os.Exit(0)
}

func setupGlfw() {
	runtime.LockOSThread()

	if err := glfw.Init(); err != nil {
		os.Exit(-1)
	}

	glfw.WindowHint(glfw.Visible, glfw.False)

	glfw.WindowHint(glfw.ContextVersionMajor, 3)
	glfw.WindowHint(glfw.ContextVersionMinor, 3)
	glfw.WindowHint(glfw.OpenGLProfile, glfw.OpenGLCoreProfile)
	glfw.WindowHint(glfw.OpenGLForwardCompatible, glfw.True)

	window, err := glfw.CreateWindow(1280, 768, "", nil, nil)

	if err != nil {
		os.Exit(-2)
	}

	window.MakeContextCurrent()
	glfw.SwapInterval(glfw.True)
	window.Show()

	if err := gl.Init(); err != nil {
		os.Exit(-3)
	}
}

func setupImGui() {
	imgui.CreateContext(nil)

	io := imgui.CurrentIO()
	io.SetIniFilename("")
	io.SetConfigFlags(imgui.ConfigFlagsDockingEnable)
}

func loop(app windowApp) {
	window := glfw.GetCurrentContext()
	ticker := time.NewTicker(time.Second / time.Duration(fps))

	for !window.ShouldClose() {
		startFrame()
		app.Loop()
		endFrame()
		<-ticker.C
	}
}

func startFrame() {
	gl.ClearColor(.25, .25, .5, 1)
	gl.Clear(gl.COLOR_BUFFER_BIT)
	platform.NewImGuiGLFWFrame()
	imgui.NewFrame()
}

func endFrame() {
	imgui.Render()
	platform.Render(imgui.RenderedDrawData())
	glfw.GetCurrentContext().SwapBuffers()
	glfw.PollEvents()
}

func disposeImGui() {
	if c, err := imgui.CurrentContext(); err == nil {
		c.Destroy()
	}
}

func disposeGlfw() {
	glfw.GetCurrentContext().Destroy()
	glfw.Terminate()
}
