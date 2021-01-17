package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond"
	"github.com/SpaiR/strongdmm/internal/app/byond/dme"
)

func (a *app) openEnvironment(file string) {
	env, err := dme.NewDme(file)
	if err != nil {
		log.Println(err)
		return
	}

	a.loadedEnvironment = env
	a.data.AddRecentEnvironment(file)
	a.updateTitle()

	a.uiPanelEnvironment.Free()

	byond.FreeDmiCache()
	byond.DmiRootDirPath = env.RootDirPath
}
