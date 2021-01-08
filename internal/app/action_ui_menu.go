package app

import "github.com/sqweek/dialog"

func (a *app) DoOpenEnvironment() {
	if file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) DoOpenEnvironmentByPath(path string) {
	a.openEnvironment(path)
}

func (a *app) DoExit() {
	a.tmpShouldClose = true
}

func (a *app) DoResetWindows() {
	a.resetWindows()
}

func (a *app) DoOpenLogs() {
	a.uiPanelLogs.Open()
}

func (a *app) RecentEnvironments() []string {
	return a.data.RecentEnvironments
}
