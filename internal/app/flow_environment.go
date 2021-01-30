package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmenv"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmicon"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmmap"
)

func (a *app) openEnvironment(file string) {
	env, err := dmenv.New(file)
	if err != nil {
		log.Println(err)
		return
	}

	a.internalData.AddRecentEnvironment(file)
	a.loadedEnvironment = env
	a.uiPanelEnvironment.Free()

	dmicon.FreeCache()
	dmicon.RootDirPath = env.RootDir
	dmmap.FreeCache()

	a.updateTitle()
}
