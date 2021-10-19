package dmmap

import (
	"sort"

	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmmap/dmminstance"
)

type Instances []*dmminstance.Instance

func InstancesFromPrefabs(prefabs dmmdata.Prefabs) Instances {
	instances := make(Instances, 0, len(prefabs))
	for _, prefab := range prefabs {
		instances = append(instances, dmminstance.New(prefab))
	}
	return instances
}

func (i Instances) PrefabsEquals(instances Instances) bool {
	if len(i) != len(instances) {
		return false
	}

	for idx, instance := range i {
		if instance.Prefab().Id() != instances[idx].Prefab().Id() {
			return false
		}
	}

	return true
}

func (i Instances) Prefabs() dmmdata.Prefabs {
	prefabs := make(dmmdata.Prefabs, 0, len(i))
	for _, instance := range i {
		prefabs = append(prefabs, instance.Prefab())
	}
	return prefabs
}

func (i Instances) Copy() Instances {
	cpy := make(Instances, len(i))
	copy(cpy, i)
	return cpy
}

func (i Instances) Sorted() Instances {
	sorted := i.Copy()
	sort.SliceStable(sorted, func(i, j int) bool {
		return dm.PathWeight(sorted[i].Prefab().Path()) < dm.PathWeight(sorted[j].Prefab().Path())
	})
	return sorted
}
