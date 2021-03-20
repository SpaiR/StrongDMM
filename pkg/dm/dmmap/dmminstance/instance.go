package dmminstance

import "github.com/SpaiR/strongdmm/pkg/dm/dmvars"

type Instance struct {
	Id   uint64
	Path string
	Vars *dmvars.Variables
}

func new(id uint64, path string, vars *dmvars.Variables) *Instance {
	return &Instance{
		Id:   id,
		Path: path,
		Vars: vars,
	}
}
