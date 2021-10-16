package dmmdata

import (
	"sdmm/dmapi/dmvars"
	"sdmm/util"
)

type Instance struct {
	id   uint64
	path string
	vars *dmvars.Variables
}

func NewInstance(id uint64, path string, vars *dmvars.Variables) *Instance {
	return &Instance{id, path, vars}
}

func (i Instance) Id() uint64 {
	if i.id == 0 {
		i.id = InstanceId(i.path, i.vars)
	}
	return i.id
}

func (i Instance) Path() string {
	return i.path
}

func (i Instance) Vars() *dmvars.Variables {
	return i.vars
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
