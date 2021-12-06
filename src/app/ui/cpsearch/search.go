package cpsearch

import (
	"strconv"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dmmap/dmminstance"
)

type App interface {
	CurrentEditor() *pmap.Editor
}

type Search struct {
	app App

	shortcuts shortcut.Shortcuts

	prefabId string

	focusedResultIdx     int
	lastFocusedResultIdx int

	results []*dmminstance.Instance
}

func (s *Search) Init(app App) {
	s.addShortcuts()
	s.app = app
	s.focusedResultIdx = -1
	s.lastFocusedResultIdx = -1
}

func (s *Search) Free() {
	s.results = nil
	s.focusedResultIdx = -1
	s.lastFocusedResultIdx = -1
}

func (s *Search) Sync() {
	s.doSearch()
}

func (s *Search) Search(prefabId uint64) {
	s.prefabId = strconv.FormatUint(prefabId, 10)
	s.doSearch()
}
