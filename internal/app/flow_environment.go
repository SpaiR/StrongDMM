package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmenv"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmicon"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap/dmminstance"
)

func (a *app) openEnvironment(file string) {
	env, err := dmenv.New(file)
	if err != nil {
		log.Println("unable to open environment: ", err)
		return
	}

	a.internalData.AddRecentEnvironment(file)
	a.loadedEnvironment = env
	a.uiPanelEnvironment.Free()

	dmicon.Cache.Free()
	dmicon.Cache.SetRootDirPath(env.RootDir)
	dmminstance.Cache.Free()

	a.updateTitle()
}
