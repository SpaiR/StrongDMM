package app

import (
	"github.com/SpaiR/strongdmm/internal/app/byond/dme"
)

func (a *app) LoadedEnvironment() *dme.Dme {
	return a.loadedEnvironment
}
