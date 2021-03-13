package app

import (
	"log"
)

func (a *app) openMap(path string) {
	a.internalData.AddRecentMap(a.loadedEnvironment.RootFile, path)
	log.Println("[app] map opened:", path)
}
