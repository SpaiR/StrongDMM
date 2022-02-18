package app

import (
	"sdmm/app/ui/dialog"
	"sdmm/env"
	"sdmm/rsc"
)

var aboutText string

func (a *app) openAboutWindow() {
	if len(aboutText) == 0 {
		aboutText = rsc.AboutTxt(env.Version, env.Revision)
	}
	dialog.Open(dialog.TypeSimple{
		Title:   "About",
		Message: aboutText,
	})
}
