package dmminstance

import (
	"log"

	"sdmm/dm/dmvars"
	"sdmm/util"
)

var Cache = &InstanceCache{instances: make(map[uint64]Instance)}

type InstanceCache struct {
	instances       map[uint64]Instance
	instancesByPath map[string][]Instance
}

func (i *InstanceCache) Free() {
	log.Printf("[dmminstance] cache free; [%d] instances disposed", len(i.instances))
	i.instances = make(map[uint64]Instance)
	i.instancesByPath = make(map[string][]Instance)
}

func (i *InstanceCache) Put(instance Instance) Instance {
	if cachedInstance, ok := i.GetById(instance.Id()); ok {
		return cachedInstance
	}
	cachedInstance := instance
	i.persist(cachedInstance)
	return cachedInstance
}

func (i *InstanceCache) Get(path string, vars *dmvars.Variables) Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := i.instances[id]; ok {
		return instance
	}
	instance := Instance{id: id, Path: path, Vars: vars}
	i.persist(instance)
	return instance
}

func (i *InstanceCache) GetById(id uint64) (Instance, bool) {
	instance, ok := i.instances[id]
	return instance, ok
}

func (i *InstanceCache) GetAllByPath(path string) []Instance {
	return i.instancesByPath[path]
}

func (i *InstanceCache) persist(instance Instance) {
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
