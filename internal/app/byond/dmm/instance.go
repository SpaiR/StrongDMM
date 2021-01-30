package dmm

import "github.com/SpaiR/strongdmm/internal/app/byond/vars"

type Instance struct {
	Id   uint64
	Path string
	Vars *vars.Variables
}

func newInstance(id uint64, path string, vars *vars.Variables) *Instance {
	return &Instance{
		Id:   id,
		Path: path,
		Vars: vars,
	}
}
