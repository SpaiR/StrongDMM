package app

import (
	"log"

	"sdmm/app/render"
	"sdmm/dm/dmenv"
	"sdmm/dm/dmicon"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmmap/dmminstance"
)

func (a *app) openEnvironment(path string) {
	log.Printf("[app] opening environment [%s]...", path)

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
	render.Free()

	a.updateTitle()

	log.Println("[app] environment opened:", path)
}

func (a *app) openMap(path string) {
	log.Printf("[app] opening map [%s]...", path)

	data, err := dmmdata.New(path)
	if err != nil {
		log.Printf("[app] unable to open map by path [%s]: %v", path, err)
		return
	}

	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
	a.internalData.Save()
	a.layout.WorkspaceArea.OpenMap(dmmap.New(a.loadedEnvironment, data))
	a.layout.Instances.Update()

	log.Println("[app] map opened:", path)
}
