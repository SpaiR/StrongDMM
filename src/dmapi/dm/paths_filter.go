package dm

import (
	"log"
	"strings"
)

type PathsFilter struct {
	findDirectChildren func(string) []string
	filteredPaths      map[string]bool
}

func NewPathsFilter(findDirectChildren func(string) []string) *PathsFilter {
	return &PathsFilter{
		findDirectChildren: findDirectChildren,
		filteredPaths:      make(map[string]bool),
	}
}

func NewPathsFilterEmpty() *PathsFilter {
	return NewPathsFilter(func(string) []string {
		return nil
	})
}

func (p *PathsFilter) Clear() {
	p.filteredPaths = make(map[string]bool)
}

func (p *PathsFilter) Copy() PathsFilter {
	filteredPaths := make(map[string]bool, len(p.filteredPaths))
	for path := range p.filteredPaths {
		filteredPaths[path] = true
	}
	return PathsFilter{
		p.findDirectChildren,
		filteredPaths,
	}
}

func (p *PathsFilter) IsHiddenPath(path string) bool {
	return p.filteredPaths[path]
}

func (p *PathsFilter) IsVisiblePath(path string) bool {
	return !p.IsHiddenPath(path)
}

func (p *PathsFilter) HasHiddenChildPath(path string) bool {
	for filteredPath := range p.filteredPaths {
		if strings.Contains(filteredPath, path) {
			return true
		}
	}
	return false
}

func (p *PathsFilter) TogglePath(path string) {
	p.togglePath(path, p.IsVisiblePath(path))
	log.Printf("[dm] toggle [%s] path: [%t]", path, p.IsVisiblePath(path))
}

func (p *PathsFilter) togglePath(path string, isFilteredOut bool) {
	for _, directChild := range p.findDirectChildren(path) {
		p.togglePath(directChild, isFilteredOut)
	}
	if isFilteredOut {
		p.filteredPaths[path] = true
	} else {
		delete(p.filteredPaths, path)
	}
}
