package app

import "github.com/SpaiR/strongdmm/internal/app/byond/dmmap"

var bdmm *dmmap.Dmm

func (a *app) openMap(path string) {
	dmmData, _ := dmmap.NewData(path)
	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
	bMap := dmmap.New(a.loadedEnvironment, dmmData)
	bdmm = bMap
}
