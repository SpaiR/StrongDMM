package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond/dme"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmi"
)

func (a *app) openEnvironment(file string) {
	env, err := dme.New(file)
	if err != nil {
		log.Println(err)
		return
	}

	a.loadedEnvironment = env
	a.data.AddRecentEnvironment(file)
	a.updateTitle()

	a.uiPanelEnvironment.Free()

	dmi.FreeCache()
	dmi.RootDirPath = env.RootDirPath
}
