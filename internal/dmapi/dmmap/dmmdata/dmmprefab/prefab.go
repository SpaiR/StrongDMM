package dmmprefab

import (
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/util"
	"sdmm/third_party/sdmmparser"
)

const (
	IdNone  = 0
	IdStage = 1 // Prefabs with this ID are temporal by their nature.
)

type Prefab struct {
	id       uint64
	path     string
	vars     *dmvars.Variables
	location sdmmparser.Location
}

func New(id uint64, path string, vars *dmvars.Variables, location sdmmparser.Location) *Prefab {
	return &Prefab{id, path, vars, location}
}

func (p Prefab) Id() uint64 {
	if p.id == IdNone {
		p.id = Id(p.path, p.vars)
	}
	return p.id
}

func (p Prefab) Path() string {
	return p.path
}

func (p Prefab) Vars() *dmvars.Variables {
	return p.vars
}

func (p Prefab) Location() sdmmparser.Location {
	return p.location
}

// Stage returns a copy of the prefab with the ID equals to IdStage. Staged prefabs are temporal.
// They are needed when creating/modifying existing prefab, without persisting of the temporal object.
func (p Prefab) Stage() Prefab {
	return Prefab{
		id:       IdStage,
		path:     p.path,
		vars:     p.vars,
		location: p.location,
	}
}

func Id(path string, vars *dmvars.Variables) uint64 {
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
