package dmmdata

import (
	"sdmm/dmapi/dmvars"
	"sdmm/util"
)

type Prefab struct {
	id   uint64
	path string
	vars *dmvars.Variables
}

func NewPrefab(id uint64, path string, vars *dmvars.Variables) *Prefab {
	return &Prefab{id, path, vars}
}

func (p Prefab) Id() uint64 {
	if p.id == 0 {
		p.id = PrefabId(p.path, p.vars)
	}
	return p.id
}

func (p Prefab) Path() string {
	return p.path
}

func (p Prefab) Vars() *dmvars.Variables {
	return p.vars
}

func PrefabId(path string, vars *dmvars.Variables) uint64 {
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
