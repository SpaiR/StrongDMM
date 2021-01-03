package app

import "github.com/sqweek/dialog"

func (a *app) DoOpenEnvironment() {
	if file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) DoExit() {
	a.tmpShouldClose = true
}
