package cpsearch

import (
	"log"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/editor"
	"strconv"

	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/util"
)

type App interface {
	CurrentEditor() *editor.Editor
	DoEditInstance(*dmminstance.Instance)
	PointSize() float32
	ShowLayout(name string, focus bool)
}

type Search struct {
	app App

	shortcuts shortcut.Shortcuts

	prefabId string

	focusedResultIdx     int
	lastFocusedResultIdx int

	filterBoundX1, filterBoundY1 int32
	filterBoundX2, filterBoundY2 int32

	resultsAll      []*dmminstance.Instance
	resultsFiltered []*dmminstance.Instance
}

func (s *Search) Init(app App) {
	s.addShortcuts()
	s.app = app
	s.focusedResultIdx = -1
	s.lastFocusedResultIdx = -1
}

func (s *Search) Free() {
	s.resultsAll = s.resultsAll[:0]
	s.focusedResultIdx = -1
	s.lastFocusedResultIdx = -1
	s.doResetFilter()
	log.Println("[cpsearch] search free")
}

func (s *Search) Sync() {
	s.doSearch()
}

func (s *Search) Search(prefabId uint64) {
	s.prefabId = strconv.FormatUint(prefabId, 10)
	s.doSearch()
}

func (s *Search) doResetFilter() {
	s.resultsFiltered = s.resultsFiltered[:0]
	s.filterBoundX1 = 0
	s.filterBoundY1 = 0
	s.filterBoundX2 = 0
	s.filterBoundY2 = 0
	log.Println("[cpsearch] search filter reset")
}

func (s *Search) updateFilteredResults() {
	s.resultsFiltered = s.resultsFiltered[:0]
	bounds := util.Bounds{
		X1: float32(s.filterBoundX1),
		Y1: float32(s.filterBoundY1),
		X2: float32(s.filterBoundX2),
		Y2: float32(s.filterBoundY2),
	}
	for _, result := range s.resultsAll {
		if bounds.Contains(float32(result.Coord().X), float32(result.Coord().Y)) {
			s.resultsFiltered = append(s.resultsFiltered, result)
		}
	}
}

func (s *Search) results() []*dmminstance.Instance {
	if len(s.resultsFiltered) > 0 {
		return s.resultsFiltered
	}
	return s.resultsAll
}
