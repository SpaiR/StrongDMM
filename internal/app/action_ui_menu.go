package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
)

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
