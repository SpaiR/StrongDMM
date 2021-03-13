package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/dm/dmenv"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmicon"
	"github.com/SpaiR/strongdmm/internal/app/dm/dmmap/dmminstance"
)

func (a *app) openEnvironment(path string) {
	env, err := dmenv.New(path)
	if err != nil {
		log.Println("[app] unable to open environment:", err)
		return
	}

	a.internalData.AddRecentEnvironment(path)
	a.loadedEnvironment = env
	a.uiPanelEnvironment.Free()

	dmicon.Cache.Free()
	dmicon.Cache.SetRootDirPath(env.RootDir)
	dmminstance.Cache.Free()

	a.updateTitle()

	log.Println("[app] environment opened:", path)
}
