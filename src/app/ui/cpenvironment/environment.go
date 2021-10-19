package cpenvironment

import (
	"fmt"
	"log"
	"strings"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

type App interface {
	LoadedEnvironment() *dmenv.Dme
	PointSize() float32
	DoSelectPrefabByPath(string)
}

// Only 25 nodes can be loaded per one process tick.
// This helps to distribute performance load between process calls.
const newTreeNodesLimit = 25

type Environment struct {
	app App

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

func (e *Environment) Process() {
	if e.app.LoadedEnvironment() == nil {
		imgui.Text("No Environment Loaded")
	} else {
		e.process()
		e.showControls()
		e.showTree()
		e.postProcess()
	}
}

func (e *Environment) showControls() {
	if imgui.Button(imguiext.IconFaMinus) {
		e.tmpDoCollapseAll = true
	}
	imguiext.SetItemHoveredTooltip("Collapse All")
	imgui.SameLine()
	imgui.SetNextItemWidth(-1)
	if imgui.InputTextWithHint("##filter", "Filter", &e.filter) {
		e.doFilter()
	}
	imgui.Separator()
}

func (e *Environment) showTree() {
	if imgui.BeginChild(fmt.Sprintf("environment_tree_[%d]", e.treeId)) {
		if len(e.filter) == 0 {
			e.showPathBranch("/area")
			e.showPathBranch("/turf")
			e.showPathBranch("/obj")
			e.showPathBranch("/mob")
		} else {
			e.showFilteredNodes()
		}
	}
	imgui.EndChild()
}

func (e *Environment) showFilteredNodes() {
	var clipper imgui.ListClipper
	clipper.Begin(len(e.filteredTreeNodes))
	for clipper.Step() {
		for i := clipper.DisplayStart; i < clipper.DisplayEnd; i++ {
			node := e.filteredTreeNodes[i]
			e.showIcon(node)
			imgui.TreeNodeV(node.orig.Path, e.nodeFlags(node, true))
			e.doSelectOnClick(node)
		}
	}
}

func (e *Environment) showPathBranch(t string) {
	if atom := e.app.LoadedEnvironment().Objects[t]; atom != nil {
		e.showBranch0(atom)
	}
}

func (e *Environment) showBranch0(object *dmenv.Object) {
	node, ok := e.newTreeNode(object)
	if ok != true {
		return
	}

	e.showIcon(node)

	if len(object.DirectChildren) == 0 {
		imgui.TreeNodeV(node.name, e.nodeFlags(node, true))
		e.scrollToSelectedPath(node)
		e.doSelectOnClick(node)
	} else {
		if e.isPartOfSelectedPath(node) {
			imgui.SetNextItemOpen(true, imgui.ConditionAlways)
		}
		if imgui.TreeNodeV(node.name, e.nodeFlags(node, false)) {
			e.scrollToSelectedPath(node)
			e.doSelectOnClick(node)

			if e.tmpDoCollapseAll {
				imgui.StateStorage().SetAllInt(0)
			}
			for _, childPath := range object.DirectChildren {
				e.showBranch0(e.app.LoadedEnvironment().Objects[childPath])
			}
			imgui.TreePop()
		} else {
			e.doSelectOnClick(node)
		}
	}
}

func (e *Environment) doSelectOnClick(node *treeNode) {
	if imgui.IsItemClicked() && e.selectedPath != node.orig.Path {
		e.app.DoSelectPrefabByPath(node.orig.Path)
		e.tmpDoSelectPath = false // we don't need to scroll tree when we select item from the tree itself
	}
}

func (e *Environment) nodeFlags(node *treeNode, leaf bool) imgui.TreeNodeFlags {
	flags := 0
	if node.orig.Path == e.selectedPath {
		flags |= int(imgui.TreeNodeFlagsSelected)
	}
	if leaf {
		flags |= int(imgui.TreeNodeFlagsLeaf | imgui.TreeNodeFlagsNoTreePushOnOpen)
	} else {
		flags |= int(imgui.TreeNodeFlagsOpenOnArrow | imgui.TreeNodeFlagsOpenOnDoubleClick)
	}
	return imgui.TreeNodeFlags(flags)
}

func (e *Environment) isPartOfSelectedPath(node *treeNode) bool {
	return e.tmpDoSelectPath && strings.HasPrefix(e.selectedPath, node.orig.Path)
}

func (e *Environment) scrollToSelectedPath(node *treeNode) {
	if e.tmpDoSelectPath && e.selectedPath == node.orig.Path {
		e.tmpDoSelectPath = false
		imgui.SetScrollHereY(.5)
	}
}

func (e *Environment) showIcon(node *treeNode) {
	s := node.sprite
	iconSize := e.iconSize()
	w.Image(imgui.TextureID(s.Texture()), iconSize, iconSize).Uv(imgui.Vec2{X: s.U1, Y: s.V1}, imgui.Vec2{X: s.U2, Y: s.V2}).Build()
	imgui.SameLine()
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
		if node, ok := e.newTreeNode(object); ok == true {
			e.filteredTreeNodes = append(e.filteredTreeNodes, node)
		}
	}

	for _, childPath := range object.DirectChildren {
		e.filterBranch0(e.app.LoadedEnvironment().Objects[childPath])
	}
}

func (e *Environment) iconSize() float32 {
	return 16 * e.app.PointSize()
}
