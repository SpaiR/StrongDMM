package app

import (
	"github.com/go-gl/glfw/v3.3/glfw"

	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	"github.com/SpaiR/strongdmm/internal/app/window"
)

const TITLE = "StrongDMM"

type app struct {
	tmpShouldClose bool

	uiMenu *ui.Menu
}

func Start() {
	app := create()

	window.ShowAndRun(TITLE, func() {
		app.process()
	})
}

func create() *app {
	app := app{}
	app.uiMenu = ui.NewMenu(&app)
	return &app
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
