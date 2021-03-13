package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
)

func (a *app) DoOpenEnvironment() {
	log.Println("[app] opening environment")
	if file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) DoOpenEnvironmentByPath(path string) {
	log.Println("[app] opening environment by path:", path)
	a.openEnvironment(path)
}

func (a *app) DoClearRecentEnvironments() {
	log.Println("[app] clearing recent environments")
	a.internalData.ClearRecentEnvironments()
	a.internalData.Save()
}

func (a *app) DoOpenMap() {
	log.Println("[app] opening map")
	if file, err := dialog.File().Title("Open Map").Filter("*.dmm", "dmm").Load(); err == nil {
		a.openMap(file)
	}
}

func (a *app) DoOpenMapByPath(path string) {
	log.Println("[app] opening map by path:", path)
	a.openMap(path)
}

func (a *app) DoClearRecentMaps() {
	log.Println("[app] clearing recent maps")
	a.internalData.ClearRecentMaps(a.loadedEnvironment.RootFile)
	a.internalData.Save()
}

func (a *app) DoExit() {
	log.Println("[app] exiting")
	a.tmpShouldClose = true
}

func (a *app) DoResetWindows() {
	log.Println("[app] resetting windows")
	a.resetWindows()
}

func (a *app) DoOpenLogs() {
	log.Println("[app] opening logs")
	if err := open.Run(a.logDir); err != nil {
		log.Println("[app] unable to open log dir:", err)
	}
}

func (a *app) RecentEnvironments() []string {
	return a.internalData.RecentEnvironments
}

func (a *app) RecentMapsByEnvironment() map[string][]string {
	return a.internalData.RecentMapsByEnvironment
}
