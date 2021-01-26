package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
)

func (a *app) DoOpenEnvironment() {
	if file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) DoOpenEnvironmentByPath(path string) {
	a.openEnvironment(path)
}

func (a *app) DoClearRecentEnvironments() {
	a.data.ClearRecentEnvironments()
}

func (a *app) DoOpenMap() {
	if file, err := dialog.File().Title("Open Map").Filter("*.dmm", "dmm").Load(); err == nil {
		a.openMap(file)
	}
}

func (a *app) DoOpenMapByPath(path string) {
	a.openMap(path)
}

func (a *app) DoClearRecentMaps() {
	a.data.ClearRecentMaps(a.loadedEnvironment.RootFilePath)
}

func (a *app) DoExit() {
	a.tmpShouldClose = true
}

func (a *app) DoResetWindows() {
	a.resetWindows()
}

func (a *app) DoOpenLogs() {
	if err := open.Run(a.logDir); err != nil {
		log.Fatal("unable to open log dir")
	}
}

func (a *app) RecentEnvironments() []string {
	return a.data.RecentEnvironments
}

func (a *app) RecentMapsByEnvironment() map[string][]string {
	return a.data.RecentMapsByEnvironment
}
