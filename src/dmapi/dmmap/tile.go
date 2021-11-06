package dmmap

import (
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type Tile struct {
	Coord     util.Point
	instances Instances
}

func (t Tile) Copy() Tile {
	return Tile{
		t.Coord,
		t.instances.DeepCopy(),
	}
}

func (t *Tile) Set(instances Instances) {
	t.instances = instances
}

func (t Tile) Instances() Instances {
	return t.instances
}

func (t *Tile) InstancesSet(prefabs dmmdata.Prefabs) {
	t.instances = InstancesFromPrefabs(prefabs)
}

func (t *Tile) InstancesAdd(prefab *dmmprefab.Prefab) {
	t.instances = append(t.instances, dmminstance.New(prefab))
}

func (t *Tile) InstancesRemoveByPath(pathToRemove string) {
	instances := make(Instances, 0, len(t.instances))
	for _, instance := range t.instances {
		if !dm.IsPath(instance.Prefab().Path(), pathToRemove) {
			instances = append(instances, instance)
		}
	}
	t.instances = instances
}

// InstancesRegenerate adds missing base prefabs, if there are some of them.
func (t *Tile) InstancesRegenerate() {
	var hasArea, hasTurf bool
	for _, instance := range t.instances {
		if dm.IsPath(instance.Prefab().Path(), "/area") {
			hasArea = true
		} else if dm.IsPath(instance.Prefab().Path(), "/turf") {
			hasTurf = true
		}
	}
	if !hasArea {
		t.InstancesAdd(baseArea)
	}
	if !hasTurf {
		t.InstancesAdd(baseTurf)
	}
}
