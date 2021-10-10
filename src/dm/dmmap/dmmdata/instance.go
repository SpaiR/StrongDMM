package dmmdata

import (
	"sdmm/dm/dmvars"
	"sdmm/util"
)

type Instance struct {
	id   uint64
	Path string
	Vars *dmvars.Variables
}

func NewInstance(id uint64, path string, vars *dmvars.Variables) *Instance {
	return &Instance{id, path, vars}
}

func (i Instance) Id() uint64 {
	if i.id == 0 {
		i.id = InstanceId(i.Path, i.Vars)
	}
	return i.id
}

func InstanceId(path string, vars *dmvars.Variables) uint64 {
	snap := path
	if vars != nil {
		for _, name := range vars.Iterate() {
			if value, ok := vars.Value(name); ok {
				snap += name + value
			} else {
				snap += name
			}
		}
	}
	return util.Djb2(snap)
}
