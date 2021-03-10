package dmminstance

import "github.com/SpaiR/strongdmm/internal/app/dm/dmvars"

var Cache = &InstanceCache{instances: make(map[uint64]*Instance)}

type InstanceCache struct {
	instances map[uint64]*Instance
}

func (i *InstanceCache) Free() {
	i.instances = make(map[uint64]*Instance)
}

func (i *InstanceCache) Get(path string, vars *dmvars.Variables) *Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := i.instances[id]; ok {
		return instance
	}
	instance := new(id, path, vars)
	i.instances[id] = instance
	return instance
}

func computeInstanceId(path string, vars *dmvars.Variables) uint64 {
	snap := path
	for _, name := range vars.Iterate() {
		if value, ok := vars.Value(name); ok {
			snap += name + value
		} else {
			snap += name
		}
	}
	return djb2(snap)
}

// http://www.cse.yorku.ca/~oz/hash.html
func djb2(str string) uint64 {
	var hash uint64 = 5381
	for _, c := range str {
		hash = ((hash << 5) + hash) + uint64(c)
	}
	return hash
}
