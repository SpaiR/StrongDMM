package cpsearch

import (
	"strconv"

	"sdmm/app/config"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmmap/dmminstance"
)

type App interface {
	CurrentEditor() *pmap.Editor
	ConfigRegister(config.Config)
	ConfigFind(string) config.Config
}

type Search struct {
	app App

	prefabId string

	results []*dmminstance.Instance
}

func (s *Search) Init(app App) {
	s.app = app
	s.loadConfig()
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
