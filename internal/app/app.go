package app

import (
	"log"
	"os"
	"runtime"

	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/data"
	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	"github.com/SpaiR/strongdmm/internal/app/window"
)

var (
	InternalDir string
)

const TITLE = "StrongDMM"

type app struct {
	tmpShouldClose bool

	data *data.InternalData

	uiMenu *ui.Menu
}

func Start() {
	app := create()

	window.ShowAndRun(TITLE, func() {
		app.process()
	})
}

func create() *app {
	findInternalDir()

	app := app{}
	app.data = data.Load(InternalDir)
	app.uiMenu = ui.NewMenu(&app)

	return &app
}

func findInternalDir() {
	userHomeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatal("unable to find user home dir")
	}
	if runtime.GOOS == "windows" {
		InternalDir = userHomeDir + "/AppData/Roaming/StrongDMM"
	} else {
		InternalDir = userHomeDir + "/.strongdmm"
	}
	_ = os.MkdirAll(InternalDir, os.ModePerm)
}

func (a *app) process() {
	shortcut.Process()

	a.uiMenu.Process()

	a.checkShouldClose()
	a.dropTmpState()
}

func (a *app) dropTmpState() {
	a.tmpShouldClose = false
}

func (a *app) checkShouldClose() {
	if a.tmpShouldClose {
		glfw.GetCurrentContext().SetShouldClose(true)
	}
}
