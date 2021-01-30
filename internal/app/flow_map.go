package app

import "github.com/SpaiR/strongdmm/internal/app/byond/dmm"

var bdmm *dmm.Dmm

func (a *app) openMap(path string) {
	dmmData, _ := dmm.NewData(path)
	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
	bMap := dmm.New(a.loadedEnvironment, dmmData)
	bdmm = bMap
}
