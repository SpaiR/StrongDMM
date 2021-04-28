package app

import (
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	"github.com/SpaiR/strongdmm/pkg/dm/dmvars"
)

func (a *app) DoSelectPath(path string) {
	empty := &dmvars.Variables{}
	empty.SetParent(a.loadedEnvironment.Objects[path].Vars)
	a.DoSelectInstance(dmminstance.Cache.Get(path, empty))
}
