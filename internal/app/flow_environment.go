package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond/dme"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmi"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmm/instance"
)

func (a *app) openEnvironment(file string) {
	env, err := dme.New(file)
	if err != nil {
		log.Println(err)
		return
	}

	a.internalData.AddRecentEnvironment(file)
	a.loadedEnvironment = env
	a.uiPanelEnvironment.Free()

	dmi.FreeCache()
	dmi.RootDirPath = env.RootDirPath
	instance.FreeCache()

	a.updateTitle()
}
