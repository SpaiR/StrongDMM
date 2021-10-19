package dmmap

import (
	"log"

	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmvars"
)

var PrefabStorage = &prefabStorage{prefabs: make(map[uint64]*dmmprefab.Prefab)}

type prefabStorage struct {
	prefabs       map[uint64]*dmmprefab.Prefab
	prefabsByPath map[string][]*dmmprefab.Prefab
}

func (s *prefabStorage) Free() {
	log.Printf("[dmmap] cache free; [%d] prefabs disposed", len(s.prefabs))
	s.prefabs = make(map[uint64]*dmmprefab.Prefab)
	s.prefabsByPath = make(map[string][]*dmmprefab.Prefab)
}

func (s *prefabStorage) Put(prefab *dmmprefab.Prefab) *dmmprefab.Prefab {
	if cachedPrefab, ok := s.GetById(prefab.Id()); ok {
		return cachedPrefab
	}
	s.persist(prefab)
	return prefab
}

func (s *prefabStorage) Get(path string, vars *dmvars.Variables) *dmmprefab.Prefab {
	id := dmmprefab.Id(path, vars)
	if prefab, ok := s.prefabs[id]; ok {
		return prefab
	}
	prefab := dmmprefab.New(id, path, vars)
	s.persist(prefab)
	return prefab
}

func (s *prefabStorage) GetById(id uint64) (*dmmprefab.Prefab, bool) {
	prefab, ok := s.prefabs[id]
	return prefab, ok
}

func (s *prefabStorage) GetAllByPath(path string) []*dmmprefab.Prefab {
	return s.prefabsByPath[path]
}

func (s *prefabStorage) persist(prefab *dmmprefab.Prefab) {
	s.prefabs[prefab.Id()] = prefab
	s.prefabsByPath[prefab.Path()] = append(s.prefabsByPath[prefab.Path()], prefab)
}
