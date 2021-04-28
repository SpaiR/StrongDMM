package app

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmmdata"
)

func (a *app) openMap(path string) {
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
