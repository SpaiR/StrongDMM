package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
)

func (a *app) AppDoExit() {
	log.Println("[app] exiting")
	a.tmpShouldClose = true
}

func (a *app) AppDoUndo() {
	log.Println("[app] undo")
	a.commandStorage.Undo()
}

func (a *app) AppDoRedo() {
	log.Println("[app] redo")
	a.commandStorage.Redo()
}

func (a *app) AppDoResetWindows() {
	log.Println("[app] resetting windows")
	a.resetWindows()
}

func (a *app) AppDoOpenLogs() {
	log.Println("[app] opening logs")
	if err := open.Run(a.logDir); err != nil {
		log.Println("[app] unable to open log dir:", err)
	}
}
