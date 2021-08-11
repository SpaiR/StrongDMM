package app

import (
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	"github.com/SpaiR/strongdmm/pkg/dm/dmvars"
)

func (a *app) AppDoSelectPath(path string) {
	empty := &dmvars.Variables{}
	empty.SetParent(a.loadedEnvironment.Objects[path].Vars)
	a.AppDoSelectInstance(dmminstance.Cache.Get(path, empty))
}
