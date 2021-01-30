package dmmap

import "github.com/SpaiR/strongdmm/internal/app/byond/dmvars"

var cache map[uint64]*Instance

func FreeCache() {
	cache = make(map[uint64]*Instance)
}

func GetInstance(path string, vars *dmvars.Variables) *Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := cache[id]; ok {
		return instance
	}
	instance := newInstance(id, path, vars)
	cache[id] = instance
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
