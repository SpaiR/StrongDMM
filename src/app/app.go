package app

import (
	"io"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/data"
	"sdmm/app/render/brush"
	"sdmm/app/ui"
	"sdmm/app/ui/shortcut"
	"sdmm/app/window"
	"sdmm/dm/dmenv"
)

const (
	Title       = "StrongDMM"
	Version     = "2.0"
	LogsTtlDays = 3
)

func Start() {
	internalDir := getOrCreateInternalDir()
	logDir := initializeLogger(internalDir)

	log.Printf("%s, v%s", Title, Version)
	log.Println("[app] starting")
	log.Println("[app] internal dir:", internalDir)
	log.Println("[app] log dir:", logDir)

	app := app{
		masterWindow: window.New(),
		logDir:       logDir,
	}

	log.Println("[app] phase: initialization")
	app.initialize(internalDir)

	log.Println("[app] phase: application loop")
	app.run()

	log.Println("[app] phase: disposing")
	app.dispose()
}

type app struct {
	masterWindow *window.Window

	logDir string

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition

	loadedEnvironment *dmenv.Dme

	internalData   *data.Internal
	commandStorage *command.Storage

	menu   *ui.Menu
	layout *ui.Layout
}

func (a *app) initialize(internalDir string) {
	a.internalData = data.LoadInternal(internalDir)
	a.commandStorage = command.NewStorage()

	a.menu = ui.NewMenu(a)
	a.layout = ui.NewLayout(a)

	a.AppUpdateTitle()
	a.resetWindows()
}

func (a *app) run() {
	a.masterWindow.Run(a.loop)
}

func (a *app) loop() {
	// FIXME: Remove
	//imgui.ShowDemoWindow(nil)

	shortcut.Process()

	a.menu.Process()
	a.layout.Process()

	a.checkShouldClose()
	a.dropTmpState()
}

func (a *app) dispose() {
	brush.Dispose()
	a.internalData.Save()
	a.masterWindow.Dispose()
}

func getOrCreateInternalDir() string {
	var internalDir string

	userHomeDir, err := os.UserHomeDir()
	if err != nil {
		log.Fatal("[app] unable to find user home dir:", err)
	}

	if runtime.GOOS == "windows" {
		internalDir = userHomeDir + "/AppData/Roaming/StrongDMM"
	} else {
		internalDir = userHomeDir + "/.strongdmm"
	}
	_ = os.MkdirAll(internalDir, os.ModePerm)

	return internalDir
}

func initializeLogger(internalDir string) string {
	// Configure logs directory.
	logDir := internalDir + "/logs"
	_ = os.MkdirAll(logDir, os.ModePerm)

	// Clear old logs
	_ = filepath.Walk(logDir, func(path string, info os.FileInfo, _ error) error {
		if time.Now().Sub(info.ModTime()).Hours()/24 > LogsTtlDays {
			_ = os.Remove(path)
		}
		return nil
	})

	// Create log file for the current session.
	formattedDate := time.Now().Format("2006.01.02-15.04.05")
	logFile := logDir + "/" + formattedDate + ".log"
	file, e := os.OpenFile(logFile, os.O_CREATE|os.O_APPEND|os.O_WRONLY, os.ModePerm)
	if e != nil {
		log.Fatal("[app] unable to open log file")
	}

	// Attach logs output to the log file and an application terminal.
	multiOut := io.MultiWriter(file, os.Stdout)
	log.SetOutput(multiOut)

	return logDir
}

func (a *app) checkShouldClose() {
	if a.tmpShouldClose {
		a.masterWindow.Handle.SetShouldClose(true)
	}
}

func (a *app) dropTmpState() {
	a.tmpShouldClose = false
	a.tmpWindowCond = imgui.ConditionFirstUseEver
}

func (a *app) resetWindows() {
	a.tmpWindowCond = imgui.ConditionAlways
	log.Println("[app] window reset")
}
