package app

import (
	"fmt"
	"io"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/data"
	"github.com/SpaiR/strongdmm/internal/app/ui"
	"github.com/SpaiR/strongdmm/internal/app/ui/shortcut"
	"github.com/SpaiR/strongdmm/internal/app/window"
	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
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
		masterWindow: window.New(window.Config{IniFilename: internalDir + "/Layout.ini"}),
		logDir:       logDir,
	}

	log.Println("[app] initializing")
	app.initialize(internalDir)

	log.Println("[app] running")
	app.run()

	log.Println("[app] disposing")
	app.dispose()
}

type app struct {
	masterWindow *window.Window

	logDir string

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition

	loadedEnvironment *dmenv.Dme

	internalData *data.Internal

	menu   *ui.Menu
	layout *ui.Layout
}

func (a *app) initialize(internalDir string) {
	a.internalData = data.LoadInternal(internalDir)

	a.menu = ui.NewMenu(a)
	a.layout = ui.NewLayout(a)

	a.updateTitle()
	a.resetWindows()
}

func (a *app) run() {
	a.masterWindow.Run(a.loop)
}

func (a *app) loop() {
	shortcut.Process()

	a.menu.Process()
	a.layout.Process()

	a.checkShouldClose()
	a.dropTmpState()
}

func (a *app) dispose() {
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
	formattedDate := time.Now().Format("2006.01.02-15.04.05")
	logDir := internalDir + "/Logs"
	_ = os.MkdirAll(logDir, os.ModePerm)

	// Clear old logs
	_ = filepath.Walk(logDir, func(path string, info os.FileInfo, _ error) error {
		if time.Now().Sub(info.ModTime()).Hours()/24 > LogsTtlDays {
			_ = os.Remove(path)
		}
		return nil
	})

	logFile := logDir + "/" + formattedDate + ".log"
	file, e := os.OpenFile(logFile, os.O_CREATE|os.O_APPEND|os.O_WRONLY, os.ModePerm)
	if e != nil {
		log.Fatal("[app] unable to open log file")
	}

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

func (a *app) updateTitle() {
	var title string

	if a.loadedEnvironment != nil {
		title = fmt.Sprintf("%s - %s", a.loadedEnvironment.Name, Title)
	} else {
		title = Title
	}

	a.masterWindow.Handle.SetTitle(title)
	log.Println("[app] title updated:", title)
}

func (a *app) resetWindows() {
	a.tmpWindowCond = imgui.ConditionAlways
	log.Println("[app] window reset")
}
