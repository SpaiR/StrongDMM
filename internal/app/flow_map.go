package app

import (
	"log"
)

func (a *app) openMap(path string) {
	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
	a.internalData.Save()
	log.Println("[app] map opened:", path)
}
