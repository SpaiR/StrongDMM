package dm

import (
	"log"

	"sdmm/util/slice"
)

type PathsFilter struct {
	excludedPaths, filteredPaths []string
}

func NewPathsFilter() *PathsFilter {
	return &PathsFilter{}
}

func (p *PathsFilter) Copy() PathsFilter {
	excludedPaths, filteredPaths := make([]string, len(p.excludedPaths)), make([]string, len(p.filteredPaths))
	copy(excludedPaths, p.excludedPaths)
	copy(filteredPaths, p.filteredPaths)
	return PathsFilter{
		excludedPaths,
		filteredPaths,
	}
}

func (p *PathsFilter) Free() {
	p.excludedPaths, p.filteredPaths = nil, nil
	log.Println("[dm] paths filter free")
}

func (p *PathsFilter) IsHiddenPath(path string) bool {
	return p.containsPath(p.filteredPaths, path) && !p.containsPath(p.excludedPaths, path)
}

func (p *PathsFilter) IsVisiblePath(path string) bool {
	return !p.IsHiddenPath(path)
}

func (p *PathsFilter) TogglePath(path string) {
	var isFiltered bool
	if idx := slice.StrIndexOf(p.filteredPaths, path); idx != -1 {
		p.filteredPaths = slice.StrRemoveIdx(p.filteredPaths, idx)
		isFiltered = false
	} else {
		p.filteredPaths = append(p.filteredPaths, path)
		isFiltered = true
	}
	log.Printf("[dm] toggle [%s] path: [%t]", path, isFiltered)
}

func (p *PathsFilter) containsPath(storage []string, path string) bool {
	for _, hiddenPath := range storage {
		if IsPath(path, hiddenPath) {
			return true
		}
	}
	return false
}
