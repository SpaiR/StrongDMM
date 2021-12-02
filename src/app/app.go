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
	"sdmm/dmapi/dmmclip"
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
		internalDir: internalDir,
		logDir:      logDir,
		backupDir:   filepath.FromSlash(internalDir + "/backup"),
	}

	a.masterWindow = window.New(&a)

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

	internalDir string
	logDir      string
	backupDir   string

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition
	tmpUpdateScale bool

	shortcutsEnabled bool

	loadedEnvironment *dmenv.Dme
	pathsFilter       *dm.PathsFilter

	configData *configData.Config
	prefsData  *prefsData.Prefs

	commandStorage *command.Storage
	clipboard      *dmmclip.Clipboard

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

	a.updateScale()
	a.updateLayoutState()

	a.commandStorage = command.NewStorage()
	a.pathsFilter = dm.NewPathsFilter()
	a.clipboard = dmmclip.New()

	a.menu = menu.New(a)
	a.layout = layout.New(a)

	a.UpdateTitle()
}

func (a *app) Process() {
	// FIXME: Remove
	//imgui.ShowDemoWindow(nil)

	if a.shortcutsEnabled {
		shortcut.Process()
	}

	a.menu.Process()
	a.layout.Process()
}

func (a *app) PostProcess() {
	a.checkShouldClose()
	a.checkUpdateScale()
	a.dropTmpState()
}

func (a *app) LayoutIniPath() string {
	return filepath.FromSlash(a.internalDir + "/layout.ini")
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
		a.masterWindow.Handle().SetShouldClose(true)
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

// Checks the version of the layout in the user config data and the app itself.
// When different, the user layout will be reset.
// Otherwise, the layout will persist its state between the app sessions.
func (a *app) updateLayoutState() {
	if a.configData.LayoutVersion != layout.Version() {
		log.Printf("[app] up layout version from [%d] to: %d", a.configData.LayoutVersion, layout.Version())
		a.resetLayout()
		a.configData.LayoutVersion = layout.Version()
		a.configData.Save()
		log.Println("[app] layout reset")
	} else if _, err := os.Stat(a.LayoutIniPath()); os.IsNotExist(err) {
		log.Println("[app] no layout was found, resetting...")
		a.resetLayout()
	} else {
		log.Println("[app] layout version is not changed")
	}
}

func (a *app) resetLayout() {
	_ = os.Remove(a.LayoutIniPath())
	log.Println("[app] layout data deleted:", a.LayoutIniPath())
	a.tmpWindowCond = imgui.ConditionAlways
	log.Println("[app] layout reset")
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
		if info != nil && !info.IsDir() && time.Now().Sub(info.ModTime()).Hours()/24 > BackupsTtlDays {
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
