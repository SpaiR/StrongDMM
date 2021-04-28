package app

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
	"github.com/SpaiR/strongdmm/pkg/dm/dmicon"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
)

func (a *app) openEnvironment(path string) {
	env, err := dmenv.New(path)
	if err != nil {
		log.Println("[app] unable to open environment:", err)
		return
	}

	a.internalData.AddRecentEnvironment(path)
	a.internalData.Save()

	a.loadedEnvironment = env
	a.layout.Instances.Free()
	a.layout.Environment.Free()
	a.layout.WorkspaceArea.Free()

	dmicon.Cache.Free()
	dmicon.Cache.SetRootDirPath(env.RootDir)
	dmminstance.Cache.Free()

	a.updateTitle()

	log.Println("[app] environment opened:", path)
}
