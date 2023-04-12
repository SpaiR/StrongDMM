package dmminstance

import (
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/util"
)

var id uint64

type Instance struct {
	id     uint64
	coord  util.Point
	prefab *dmmprefab.Prefab
}

func (i *Instance) SetPrefab(prefab *dmmprefab.Prefab) {
	i.prefab = prefab
}

func (i Instance) Copy() Instance {
	return Instance{
		id:     i.id,
		coord:  i.coord,
		prefab: i.prefab,
	}
}

func (i Instance) Id() uint64 {
	return i.id
}

func (i Instance) Coord() util.Point {
	return i.coord
}

func (i Instance) Prefab() *dmmprefab.Prefab {
	return i.prefab
}

func New(coord util.Point, prefab *dmmprefab.Prefab) *Instance {
	id++
	return &Instance{
		id,
		coord,
		prefab,
	}
}
