package dmminstance

import (
	"sdmm/dm/dmvars"
)

type Instance struct {
	id   uint64
	Path string
	Vars *dmvars.Variables
}

func newInstance(id uint64, path string, vars *dmvars.Variables) *Instance {
	return &Instance{
		id:   id,
		Path: path,
		Vars: vars,
	}
}

func (i *Instance) Id() uint64 {
	if i.id == 0 {
		i.id = computeInstanceId(i.Path, i.Vars)
	}
	return i.id
}
