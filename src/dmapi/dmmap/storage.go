package dmmap

import (
	"log"

	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/dmapi/dmvars"
)

var PrefabStorage = &prefabStorage{prefabs: make(map[uint64]*dmmdata.Prefab)}

type prefabStorage struct {
	prefabs       map[uint64]*dmmdata.Prefab
	prefabsByPath map[string][]*dmmdata.Prefab
}

func (s *prefabStorage) Free() {
	log.Printf("[dmmap] cache free; [%d] prefabs disposed", len(s.prefabs))
	s.prefabs = make(map[uint64]*dmmdata.Prefab)
	s.prefabsByPath = make(map[string][]*dmmdata.Prefab)
}

func (s *prefabStorage) Put(prefab *dmmdata.Prefab) *dmmdata.Prefab {
	if cachedPrefab, ok := s.GetById(prefab.Id()); ok {
		return cachedPrefab
	}
	s.persist(prefab)
	return prefab
}

func (s *prefabStorage) Get(path string, vars *dmvars.Variables) *dmmdata.Prefab {
	id := dmmdata.PrefabId(path, vars)
	if prefab, ok := s.prefabs[id]; ok {
		return prefab
	}
	prefab := dmmdata.NewPrefab(id, path, vars)
	s.persist(prefab)
	return prefab
}

func (s *prefabStorage) GetById(id uint64) (*dmmdata.Prefab, bool) {
	prefab, ok := s.prefabs[id]
	return prefab, ok
}

func (s *prefabStorage) GetAllByPath(path string) []*dmmdata.Prefab {
	return s.prefabsByPath[path]
}

func (s *prefabStorage) persist(prefab *dmmdata.Prefab) {
	s.prefabs[prefab.Id()] = prefab
	s.prefabsByPath[prefab.Path()] = append(s.prefabsByPath[prefab.Path()], prefab)
}
