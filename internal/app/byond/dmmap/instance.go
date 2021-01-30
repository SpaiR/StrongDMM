package dmmap

import "github.com/SpaiR/strongdmm/internal/app/byond/dmvars"

type Instance struct {
	Id   uint64
	Path string
	Vars *dmvars.Variables
}

func newInstance(id uint64, path string, vars *dmvars.Variables) *Instance {
	return &Instance{
		Id:   id,
		Path: path,
		Vars: vars,
	}
}
