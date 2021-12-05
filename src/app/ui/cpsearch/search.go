package cpsearch

import (
	"strconv"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmmap/dmminstance"
)

type App interface {
	CurrentEditor() *pmap.Editor
}

type Search struct {
	app App

	prefabId string

	results []*dmminstance.Instance
}

func (s *Search) Init(app App) {
	s.app = app
}

func (s *Search) Free() {
	s.results = nil
}

func (s *Search) Sync() {
	s.doSearch()
}

func (s *Search) Search(prefabId uint64) {
	s.prefabId = strconv.FormatUint(prefabId, 10)
	s.doSearch()
}

func (s *Search) doSearch() {
	if len(s.prefabId) == 0 {
		return
	}

	prefabId, err := strconv.ParseUint(s.prefabId, 10, 64)
	if err != nil {
		return
	}

	s.results = s.app.CurrentEditor().FindInstancesByPrefabId(prefabId)
}
