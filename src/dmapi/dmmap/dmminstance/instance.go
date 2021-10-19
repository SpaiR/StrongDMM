package dmminstance

import (
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
)

var instanceCount uint64

type Instance struct {
	id     uint64
	prefab *dmmprefab.Prefab
}

func (i Instance) Duplicate() *Instance {
	return New(i.prefab)
}

func (i Instance) Id() uint64 {
	return i.id
}

func (i Instance) Prefab() *dmmprefab.Prefab {
	return i.prefab
}

func New(prefab *dmmprefab.Prefab) *Instance {
	instanceCount++
	return &Instance{
		id:     instanceCount,
		prefab: prefab,
	}
}
