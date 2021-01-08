package app

import (
	"log"

	"github.com/SpaiR/strongdmm/internal/app/byond"
)

func (a *app) openEnvironment(file string) {
	env, err := byond.NewDme(file)
	if err != nil {
		log.Println(err)
		return
	}

	a.loadedEnvironment = env
	a.data.AddRecentEnvironment(file)
	a.updateTitle()
}
