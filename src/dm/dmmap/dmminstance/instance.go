package dmminstance

import (
	"sdmm/dm/dmvars"
)

type Instance struct {
	id   uint64
	Path string
	Vars *dmvars.Variables
}

func (i Instance) Id() uint64 {
	if i.id == 0 {
		i.id = computeInstanceId(i.Path, i.Vars)
	}
	return i.id
}
