package app

import (
	"io"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"sdmm/internal/app/command"
	"sdmm/internal/app/config"
	"sdmm/internal/app/render/brush"
	"sdmm/internal/app/ui/dialog"
	"sdmm/internal/app/ui/layout"
	"sdmm/internal/app/ui/menu"
	"sdmm/internal/app/ui/shortcut"
	"sdmm/internal/app/window"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmclip"
	"sdmm/internal/env"

	"github.com/SpaiR/imgui-go"
	"github.com/matishsiao/goInfo"
)

const (
	ttlDaysLogs    = 14
	ttlDaysBackups = 3
)

func Start() {
	internalDir := getOrCreateInternalDir()
	logDir := initializeLogger(internalDir)

	log.Printf("%s, %s", env.Title, env.Version)

	if osInfo, err := goInfo.GetInfo(); err == nil {
		log.Println("Kernel:", osInfo.Kernel)
		log.Println("Core:", osInfo.Core)
		log.Println("Platform:", osInfo.Platform)
		log.Println("OS:", osInfo.OS)
		log.Println("CPUs:", osInfo.CPUs)
		log.Println("Runtime:", runtime.Version())
	}

	log.Println("[app] starting")
	log.Println("[app] internal dir:", internalDir)
	log.Println("[app] log dir:", logDir)

	a := app{
		internalDir: internalDir,
		logDir:      logDir,
		backupDir:   filepath.FromSlash(internalDir + "/backup"),
		configDir:   filepath.FromSlash(internalDir + "/config"),
	}

	a.masterWindow = window.New(&a)

	log.Println("[app] start phase: [initialize]")
	a.initialize()
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
	configDir   string

	tmpShouldClose bool
	tmpWindowCond  imgui.Condition
	tmpUpdateScale bool

	// ...omae wa mou shindeiru...
	// Should be modified only in the CloseCheck method. Ensures we made everything before the closing.
	closed bool

	shortcutsEnabled bool

	loadedEnvironment *dmenv.Dme
	pathsFilter       *dm.PathsFilter

	configs map[string]config.Config

	commandStorage *command.Storage
	clipboard      *dmmclip.Clipboard

	menu   *menu.Menu
	layout *layout.Layout
}

func (a *app) initialize() {
	a.deleteOldLogs()
	a.deleteOldBackups()

	a.loadConfig()
	a.loadProjectConfig()
	a.loadPreferencesConfig()

	a.runBackgroundConfigSave()

	a.shortcutsEnabled = true

	a.commandStorage = command.NewStorage()
	a.pathsFilter = dm.NewPathsFilterEmpty()
	a.clipboard = dmmclip.New()

	a.menu = menu.New(a)
	a.layout = layout.New(a)

	a.updateScale()
	a.updateLayoutState()

	a.UpdateTitle()

	a.checkProgramArgs()

	if a.preferencesConfig().Application.CheckForUpdates {
		go a.checkForUpdates()
	}
}

func (a *app) Process() {
	if a.shortcutsEnabled {
		shortcut.Process()
	}

	a.menu.Process()
	a.layout.Process()

	dialog.Process()
}

func (a *app) PostProcess() {
	a.checkShouldClose()
	a.checkUpdateScale()
	a.dropTmpState()
}

func (a *app) CloseCheck() {
	log.Println("[app] run close check")
	a.layout.WsArea.CloseAllMaps(func(closed bool) {
		a.closed = closed
	})
}

func (a *app) IsClosed() bool {
	return a.closed
}

func (a *app) LayoutIniPath() string {
	return filepath.FromSlash(a.internalDir + "/layout.ini")
}

func (a *app) dispose() {
	brush.Dispose()
	a.configSave()
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
	file, err := os.OpenFile(logFile, os.O_CREATE|os.O_APPEND|os.O_WRONLY, os.ModePerm)
	if err != nil {
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
	window.SetPointSize(float32(a.preferencesConfig().Prefs.Interface.Scale) / 100)
}

// Checks the version of the layout in the user config data and the app itself.
// When different, the user layout will be reset.
// Otherwise, the layout will persist its state between the app sessions.
func (a *app) updateLayoutState() {
	if a.layout.CheckLayoutState() {
		a.layout.SyncLayoutState()
		log.Println("[app] reset layout state")
		a.resetLayout()
	} else if _, err := os.Stat(a.LayoutIniPath()); os.IsNotExist(err) {
		log.Println("[app] no layout was found, resetting...")
		a.resetLayout()
	} else {
		log.Println("[app] layout state is not changed")
	}
}

func (a *app) resetLayout() {
	_ = os.Remove(a.LayoutIniPath())
	log.Println("[app] layout data deleted:", a.LayoutIniPath())
	a.tmpWindowCond = imgui.ConditionAlways
	log.Println("[app] layout reset")
}

func (a *app) deleteOldLogs() {
	logsCount := deleteOldFiles(a.logDir, ttlDaysLogs)
	if logsCount > 0 {
		log.Println("[app] old logs deleted:", logsCount)
	} else {
		log.Println("[app] no old logs to delete")
	}
}

func (a *app) deleteOldBackups() {
	backupsCount := deleteOldFiles(a.backupDir, ttlDaysBackups)
	if backupsCount > 0 {
		log.Println("[app] old backups deleted:", backupsCount)
	} else {
		log.Println("[app] no old backups to delete")
	}
}

func deleteOldFiles(dir string, ttlDays float64) (deletedFiles int) {
	_ = filepath.Walk(dir, func(path string, info os.FileInfo, _ error) error {
		if info != nil && !info.IsDir() && time.Since(info.ModTime()).Hours()/24 > ttlDays {
			if err := os.Remove(path); err == nil {
				log.Println("[app] old file deleted:", path)
				deletedFiles++
			} else {
				log.Printf("[app] unable to delete old file [%s]: %v", path, err)
			}
		}
		return nil
	})
	return
}
