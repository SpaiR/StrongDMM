package app

import (
	"io"
	"log"
	"math/rand"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	configData "sdmm/app/data/config"
	prefsData "sdmm/app/data/prefs"
	"sdmm/app/render/brush"
	"sdmm/app/ui/layout"
	"sdmm/app/ui/menu"
	"sdmm/app/ui/shortcut"
	"sdmm/app/window"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap"
)

const (
	Title   = "StrongDMM"
	Version = "2.0.0.alpha"

	LogsTtlDays    = 3
	BackupsTtlDays = 3
)

func Start() {
	internalDir := getOrCreateInternalDir()
	logDir := initializeLogger(internalDir)

	log.Printf("%s, v%s", Title, Version)
	log.Println("[app] starting")
	log.Println("[app] internal dir:", internalDir)
	log.Println("[app] log dir:", logDir)

	a := app{
		logDir:    logDir,
		backupDir: filepath.FromSlash(internalDir + "/backup"),
	}

	a.masterWindow = window.New(a.process, a.postProcess)

	log.Println("[app] start phase: [initialize]")
	a.initialize(internalDir)
	log.Println("[app] end phase: [initialize]")

	log.Println("[app] start phase: [process]")
	a.masterWindow.Process()
	log.Println("[app] end phase: [process]")

	log.Println("[app] start phase: [dispose]")
	a.dispose()
	log.Println("[app] end phase: [dispose]")
}

type app struct {
	masterWindow *window.Window

	logDir    string
	backupDir string

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition
	tmpUpdateScale bool

	shortcutsEnabled bool

	loadedEnvironment *dmenv.Dme
	pathsFilter       *dm.PathsFilter

	configData *configData.Config
	prefsData  *prefsData.Prefs

	commandStorage *command.Storage
	clipboard      *dmmap.Clipboard

	menu   *menu.Menu
	layout *layout.Layout
}

func (a *app) initialize(internalDir string) {
	rand.Seed(time.Now().UnixNano())

	a.deleteOldLogs()
	a.deleteOldBackups()

	a.configData = configData.Load(internalDir)
	a.prefsData = prefsData.Load(internalDir)

	a.shortcutsEnabled = true

	a.commandStorage = command.NewStorage()
	a.pathsFilter = dm.NewPathsFilter()
	a.clipboard = dmmap.NewClipboard(a.pathsFilter)

	a.menu = menu.New(a)
	a.layout = layout.New(a)

	a.UpdateTitle()
	a.updateScale()
	a.resetWindows()
}

func (a *app) process() {
	// FIXME: Remove
	//imgui.ShowDemoWindow(nil)

	if a.shortcutsEnabled {
		shortcut.Process()
	}

	a.menu.Process()
	a.layout.Process()
}

func (a *app) postProcess() {
	a.checkShouldClose()
	a.checkUpdateScale()
	a.dropTmpState()
}

func (a *app) dispose() {
	brush.Dispose()
	a.configData.Save()
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

	return filepath.FromSlash(internalDir)
}

func initializeLogger(internalDir string) string {
	// Configure logs directory.
	logDir := internalDir + "/logs"
	_ = os.MkdirAll(logDir, os.ModePerm)

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

	return filepath.FromSlash(logDir)
}

func (a *app) checkShouldClose() {
	if a.tmpShouldClose {
		a.masterWindow.Handle.SetShouldClose(true)
	}
}

func (a *app) checkUpdateScale() {
	if a.tmpUpdateScale {
		a.updateScale()
	}
}

func (a *app) dropTmpState() {
	a.tmpShouldClose = false
	a.tmpWindowCond = imgui.ConditionFirstUseEver
	a.tmpUpdateScale = false
}

func (a *app) updateScale() {
	a.masterWindow.SetPointSize(float32(a.prefsData.Scale) / 100)
}

func (a *app) resetWindows() {
	a.tmpWindowCond = imgui.ConditionAlways
	log.Println("[app] window reset")
}

func (a *app) deleteOldLogs() {
	logsCount := 0
	_ = filepath.Walk(a.logDir, func(path string, info os.FileInfo, _ error) error {
		if time.Now().Sub(info.ModTime()).Hours()/24 > LogsTtlDays {
			_ = os.Remove(path)
			logsCount++
		}
		return nil
	})
	if logsCount > 0 {
		log.Println("[app] old logs deleted:", logsCount)
	} else {
		log.Println("[app] no old logs to delete")
	}
}

func (a *app) deleteOldBackups() {
	backupsCount := 0
	_ = filepath.Walk(a.backupDir, func(path string, info os.FileInfo, _ error) error {
		if !info.IsDir() && time.Now().Sub(info.ModTime()).Hours()/24 > BackupsTtlDays {
			_ = os.Remove(path)
			backupsCount++
		}
		return nil
	})
	if backupsCount > 0 {
		log.Println("[app] old backups deleted:", backupsCount)
	} else {
		log.Println("[app] no old backups to delete")
	}
}
