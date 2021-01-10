package app

import "github.com/SpaiR/strongdmm/internal/app/byond"

func (a *app) LoadedEnvironment() *byond.Dme {
	return a.loadedEnvironment
}
