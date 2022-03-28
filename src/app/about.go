package app

import (
	"fmt"
	"log"
	"sdmm/app/ui/dialog"
	"sdmm/env"
	w "sdmm/imguiext/widget"
	"sdmm/platform"
	"sdmm/rsc"
)

var aboutText string

func (a *app) openAboutWindow() {
	if len(aboutText) == 0 {
		aboutText = rsc.AboutTxt(env.Version)
	}
	dialog.Open(dialog.TypeCustom{
		Title:       "About",
		CloseButton: true,
		Layout: w.Layout{
			w.Text(aboutText),
			w.NewLine(),
			w.AlignTextToFramePadding(),
			w.Text("Revision:"),
			w.SameLine(),
			w.Button(env.Revision, func() {
				link := fmt.Sprintf("%s/tree/%s", env.GitHub, env.Revision)
				log.Println("[app] copy revision link:", link)
				platform.SetClipboard(link) // open in browser doesn't work for some reason
			}).Tooltip("Copy link to clipboard").Small(true),
		},
	})
}
