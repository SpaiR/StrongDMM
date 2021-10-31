package dmminstance

import (
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
)

var id uint64

type Instance struct {
	id     uint64
	prefab *dmmprefab.Prefab
}

func (i *Instance) SetPrefab(prefab *dmmprefab.Prefab) {
	i.prefab = prefab
}

func (i Instance) Copy() Instance {
	return Instance{
		id:     i.id,
		prefab: i.prefab,
	}
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
	id++
	return &Instance{
		id:     id,
		prefab: prefab,
	}
}
