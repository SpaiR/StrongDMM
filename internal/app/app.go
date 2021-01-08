package app

import (
	"fmt"
	"log"
	"os"
	"runtime"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/byond"
	"github.com/SpaiR/strongdmm/internal/app/data"
	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	"github.com/SpaiR/strongdmm/internal/app/window"
)

func Start() {
	internalDir := getOrCreateInternalDir()

	app := app{
		masterWindow: window.New(window.Config{IniFilename: internalDir + "/Layout.ini"}),
	}

	app.initialize(internalDir)
	app.run()
	app.dispose()
}

const TITLE = "StrongDMM"

type app struct {
	masterWindow *window.Window

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition

	loadedEnvironment *byond.Dme

	data *data.InternalData

	uiMenu      *ui.Menu
	uiLayout    *ui.Layout
	uiPanelLogs *ui.Logs
}

func (a *app) initialize(internalDir string) {
	a.data = data.Load(internalDir)

	a.uiMenu = ui.NewMenu(a)
	a.uiLayout = ui.NewLayout(a)
	a.uiPanelLogs = ui.NewLogs(a)

	a.updateTitle()
	a.resetWindows()
}

func (a *app) run() {
	a.masterWindow.Run(a.loop)
}

func (a *app) loop() {
	shortcut.Process()

	a.uiMenu.Process()
	a.uiLayout.Process()
	a.uiPanelLogs.Process()

	a.checkShouldClose()
	a.dropTmpState()
}

func (a *app) dispose() {
	a.data.Save()
	a.masterWindow.Dispose()
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
	a.tmpWindowCond = imgui.ConditionFirstUseEver
}

func (a *app) checkShouldClose() {
	if a.tmpShouldClose {
		a.masterWindow.Handle.SetShouldClose(true)
	}
}

func (a *app) updateTitle() {
	var title string

	if a.loadedEnvironment != nil {
		title = fmt.Sprintf("%s - %s", a.loadedEnvironment.Name, TITLE)
	} else {
		title = TITLE
	}

	a.masterWindow.Handle.SetTitle(title)
}

func (a *app) resetWindows() {
	a.tmpWindowCond = imgui.ConditionAlways
}
