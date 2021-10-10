package dmmap

import (
	"log"

	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmvars"
)

var InstanceCache = &instanceCache{instances: make(map[uint64]*dmmdata.Instance)}

type instanceCache struct {
	instances       map[uint64]*dmmdata.Instance
	instancesByPath map[string][]*dmmdata.Instance
}

func (i *instanceCache) Free() {
	log.Printf("[dmmap] cache free; [%d] instances disposed", len(i.instances))
	i.instances = make(map[uint64]*dmmdata.Instance)
	i.instancesByPath = make(map[string][]*dmmdata.Instance)
}

func (i *instanceCache) Put(instance *dmmdata.Instance) *dmmdata.Instance {
	if cachedInstance, ok := i.GetById(instance.Id()); ok {
		return cachedInstance
	}
	i.persist(instance)
	return instance
}

func (i *instanceCache) Get(path string, vars *dmvars.Variables) *dmmdata.Instance {
	id := dmmdata.InstanceId(path, vars)
	if instance, ok := i.instances[id]; ok {
		return instance
	}
	instance := dmmdata.NewInstance(id, path, vars)
	i.persist(instance)
	return instance
}

func (i *instanceCache) GetById(id uint64) (*dmmdata.Instance, bool) {
	instance, ok := i.instances[id]
	return instance, ok
}

func (i *instanceCache) GetAllByPath(path string) []*dmmdata.Instance {
	return i.instancesByPath[path]
}

func (i *instanceCache) persist(instance *dmmdata.Instance) {
	i.instances[instance.Id()] = instance
	i.instancesByPath[instance.Path()] = append(i.instancesByPath[instance.Path()], instance)
}
