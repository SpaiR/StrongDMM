package app

import "github.com/SpaiR/strongdmm/internal/app/byond/dmm"

func (a *app) openMap(path string) {
	dmmData, _ := dmm.NewData(path)
	println(dmmData)
}
