package dmminstance

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmvars"
	"github.com/SpaiR/strongdmm/pkg/util"
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

func (i *InstanceCache) Get(path string, vars *dmvars.Variables) *Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := i.instances[id]; ok {
		return instance
	}
	instance := i.makeInstance(id, path, vars)
	i.instances[id] = instance
	return instance
}

func (i *InstanceCache) GetById(id uint64) *Instance {
	return i.instances[id]
}

func (i *InstanceCache) GetByPath(path string) []*Instance {
	return i.instancesByPath[path]
}

func (i *InstanceCache) makeInstance(id uint64, path string, vars *dmvars.Variables) *Instance {
	instance := newInstance(id, path, vars)
	i.storeInstance(instance)
	return instance
}

func (i *InstanceCache) storeInstance(instance *Instance) {
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
