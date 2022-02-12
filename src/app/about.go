package app

import (
	"sdmm/app/ui/dialog"
	"sdmm/rsc"
)

var aboutText string

func (a *app) openAboutWindow() {
	if len(aboutText) == 0 {
		aboutText = rsc.AboutTxt(Version, "unknown")
	}
	dialog.Open(dialog.TypeSimple{
		Title:   "About",
		Message: aboutText,
	})
}
