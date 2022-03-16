package cpenvironment

import (
	"github.com/SpaiR/imgui-go"
	"log"
	"sdmm/app/ui/shortcut"
	"sdmm/dmapi/dm"
	"strings"

	"sdmm/dmapi/dmenv"
)

type App interface {
	LoadedEnvironment() *dmenv.Dme
	DoSelectPrefabByPath(string)
	DoEditPrefabByPath(string)
	DoSearchPrefab(prefabId uint64)
	HasActiveMap() bool
	ShowLayout(name string, focus bool)
	PathsFilter() *dm.PathsFilter
}

// Only 25 nodes can be loaded per one process tick.
// This helps to distribute performance load between process calls.
const newTreeNodesLimit = 25

type Environment struct {
	app App

	shortcuts shortcut.Shortcuts

	typesFilterEnabled bool

	treeId uint

	treeNodes         map[string]*treeNode
	filteredTreeNodes []*treeNode

	filter       string
	selectedPath string

	tmpNewTreeNodesCount int
	tmpDoRepeatFilter    bool
	tmpDoCollapseAll     bool
	tmpDoSelectPath      bool
}

func (e *Environment) Init(app App) {
	e.addShortcuts()
	e.app = app
	e.treeNodes = make(map[string]*treeNode)
}

func (e *Environment) Free() {
	e.treeId++
	e.treeNodes = make(map[string]*treeNode)
	e.filteredTreeNodes = nil
	e.filter = ""
	e.selectedPath = ""
	log.Println("[cpenvironment] environment panel free")
}

func (e *Environment) process() {
	e.tmpNewTreeNodesCount = 0

	if e.tmpDoRepeatFilter {
		e.doFilter()
	}
}

func (e *Environment) postProcess() {
	e.tmpDoCollapseAll = false
}

func (e *Environment) SelectPath(path string) {
	if path != e.selectedPath {
		log.Printf("[cpenvironment] environment path selected: [%s]", path)
		e.selectedPath = path
		e.tmpDoSelectPath = true
	}
}

func (e *Environment) doFilter() {
	e.filteredTreeNodes = nil

	if len(e.filter) == 0 {
		return
	}

	initialNewTreeNodesCount := e.tmpNewTreeNodesCount

	e.filterPathBranch("/area")
	e.filterPathBranch("/turf")
	e.filterPathBranch("/obj")
	e.filterPathBranch("/mob")

	e.tmpDoRepeatFilter = initialNewTreeNodesCount != e.tmpNewTreeNodesCount
}

func (e *Environment) filterPathBranch(t string) {
	if atom := e.app.LoadedEnvironment().Objects[t]; atom != nil {
		e.filterBranch0(atom)
	}
}

func (e *Environment) filterBranch0(object *dmenv.Object) {
	if strings.Contains(object.Path, e.filter) {
		if node, ok := e.newTreeNode(object); ok {
			e.filteredTreeNodes = append(e.filteredTreeNodes, node)
		}
	}

	for _, childPath := range object.DirectChildren {
		e.filterBranch0(e.app.LoadedEnvironment().Objects[childPath])
	}
}

func (e *Environment) iconSize() float32 {
	return imgui.FrameHeight()
}

func (e *Environment) doCollapseAll() {
	log.Println("[cpenvironment] do collapse all")
	e.tmpDoCollapseAll = true
}

func (e *Environment) doToggleTypesFilter() {
	e.typesFilterEnabled = !e.typesFilterEnabled
	log.Println("[cpenvironment] do toggle types filter:", e.typesFilterEnabled)
}
