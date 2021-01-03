package app

import "github.com/SpaiR/strongdmm/internal/app/byond"

func (a *app) openEnvironment(file string) {
	byond.NewDme(file)
}
