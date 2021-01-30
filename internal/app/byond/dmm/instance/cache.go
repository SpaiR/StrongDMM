package instance

var cache map[uint64]*Instance

func FreeCache() {
	cache = make(map[uint64]*Instance)
}

func Get(path string, vars map[string]string) *Instance {
	id := computeInstanceId(path, vars)
	if instance, ok := cache[id]; ok {
		return instance
	}
	instance := New(id, path, vars)
	cache[id] = instance
	return instance
}

func computeInstanceId(path string, vars map[string]string) uint64 {
	snap := path
	if vars != nil && len(vars) > 0 {
		for key, value := range vars {
			snap += key + value
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
