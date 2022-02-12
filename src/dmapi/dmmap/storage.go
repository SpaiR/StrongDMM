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

// Put persists the provided prefab in the storage.
func (s *prefabStorage) Put(prefab *dmmprefab.Prefab) *dmmprefab.Prefab {
	if cachedPrefab, ok := s.GetById(prefab.Id()); ok {
		return cachedPrefab
	}
	if prefab.Id() != dmmprefab.IdStage { // Ignore staged prefabs.
		s.persist(prefab)
	}
	return prefab
}

// Initial returns a prefab with an initial state (initial prefabs).
func (s *prefabStorage) Initial(path string) *dmmprefab.Prefab {
	return s.Get(path, dmvars.FromParent(environment.Objects[path].Vars))
}

// Get returns a prefab for the provided path and variables.
func (s *prefabStorage) Get(path string, vars *dmvars.Variables) *dmmprefab.Prefab {
	p, _ := s.GetV(path, vars)
	return p
}

// GetV returns a prefab for the provided path and variables.
// Same as Get but has the second argument which shows if the prefab was created.
func (s *prefabStorage) GetV(path string, vars *dmvars.Variables) (*dmmprefab.Prefab, bool) {
	id := dmmprefab.Id(path, vars)
	if prefab, ok := s.prefabs[id]; ok {
		return prefab, false
	}
	prefab := dmmprefab.New(id, path, vars)
	s.persist(prefab)
	return prefab, true
}

// Delete deletes the provided prefab from the storage.
func (s *prefabStorage) Delete(prefab *dmmprefab.Prefab) {
	delete(s.prefabs, prefab.Id())
	for idx, p := range s.prefabsByPath[prefab.Path()] {
		if p.Id() == prefab.Id() {
			s.prefabsByPath[prefab.Path()] = append(s.prefabsByPath[prefab.Path()][:idx], s.prefabsByPath[prefab.Path()][idx+1:]...)
			break
		}
	}
}

// GetById returns a prefab by the provided id. If the prefab is a null, the second return value will be a "false".
func (s *prefabStorage) GetById(id uint64) (*dmmprefab.Prefab, bool) {
	prefab, ok := s.prefabs[id]
	return prefab, ok
}

// GetAllByPath returns a slice of prefabs for the provided path.
func (s *prefabStorage) GetAllByPath(path string) []*dmmprefab.Prefab {
	return s.prefabsByPath[path]
}

func (s *prefabStorage) persist(prefab *dmmprefab.Prefab) {
	s.prefabs[prefab.Id()] = prefab
	s.prefabsByPath[prefab.Path()] = append(s.prefabsByPath[prefab.Path()], prefab)
}
