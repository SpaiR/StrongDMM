package app

import (
	"fmt"
	"log"
	"os"
	"runtime"

	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/byond"
	"github.com/SpaiR/strongdmm/internal/app/data"
	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	"github.com/SpaiR/strongdmm/internal/app/window"
)

func Start() {
	app := app{}
	window.ShowAndRun(&app)
}

const TITLE = "StrongDMM"

type app struct {
	tmpShouldClose bool

	loadedEnvironment *byond.Dme

	data *data.InternalData

	uiMenu *ui.Menu
}

func (a *app) Initialize() {
	internalDir := getOrCreateInternalDir()

	a.data = data.Load(internalDir)
	a.uiMenu = ui.NewMenu(a)

	a.updateTitle()
}

func (a *app) Loop() {
	shortcut.Process()

	a.uiMenu.Process()

	a.checkShouldClose()
	a.dropTmpState()
}

func (a *app) Dispose() {
	a.data.Save()
}

func getOrCreateInternalDir() string {
	var internalDir string

	userHomeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatal("unable to find user home dir")
	}

	if runtime.GOOS == "windows" {
		internalDir = userHomeDir + "/AppData/Roaming/StrongDMM"
	} else {
		internalDir = userHomeDir + "/.strongdmm"
	}
	_ = os.MkdirAll(internalDir, os.ModePerm)

	return internalDir
}

func (a *app) dropTmpState() {
	a.tmpShouldClose = false
}

func (a *app) checkShouldClose() {
	if a.tmpShouldClose {
		glfw.GetCurrentContext().SetShouldClose(true)
	}
}

func (a *app) updateTitle() {
	var title string

	if a.loadedEnvironment != nil {
		title = fmt.Sprintf("%s - %s", a.loadedEnvironment.Name, TITLE)
	} else {
		title = TITLE
	}

	glfw.GetCurrentContext().SetTitle(title)
}
