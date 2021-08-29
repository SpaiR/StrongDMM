package dmminstance

import (
	"log"

	"sdmm/dm/dmvars"
	"sdmm/util"
)

var Cache = &InstanceCache{instances: make(map[uint64]*Instance)}

type InstanceCache struct {
	instances       map[uint64]*Instance
	instancesByPath map[string][]*Instance
}

func (i *InstanceCache) Free() {
	log.Printf("[dmminstance] cache free; [%d] instances disposed", len(i.instances))
	i.instances = make(map[uint64]*Instance)
	i.instancesByPath = make(map[string][]*Instance)
}

func (i *InstanceCache) Put(instance Instance) *Instance {
	if cachedInstance := i.GetById(instance.Id()); cachedInstance != nil {
		return cachedInstance
	}
	cachedInstance := &instance
	i.persist(cachedInstance)
	return cachedInstance
}

func (i *InstanceCache) Get(path string, vars *dmvars.Variables) *Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := i.instances[id]; ok {
		return instance
	}
	instance := newInstance(id, path, vars)
	i.persist(instance)
	return instance
}

func (i *InstanceCache) GetById(id uint64) *Instance {
	return i.instances[id]
}

func (i *InstanceCache) GetByPath(path string) []*Instance {
	return i.instancesByPath[path]
}

func (i *InstanceCache) persist(instance *Instance) {
	i.instances[instance.Id()] = instance
	i.instancesByPath[instance.Path] = append(i.instancesByPath[instance.Path], instance)
}

func computeInstanceId(path string, vars *dmvars.Variables) uint64 {
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
