package ui

import (
	"strings"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/byond"
)

type environmentAction interface {
	LeftNodeId() int
	LoadedEnvironment() *byond.Dme
}

type Environment struct {
	action environmentAction

	treeNodes         map[string]*treeNode
	filteredTreeNodes []*treeNode

	filter string
}

func NewEnvironment(action environmentAction) *Environment {
	return &Environment{
		action:    action,
		treeNodes: make(map[string]*treeNode),
	}
}

func (e *Environment) Process() {
	imgui.DockBuilderDockWindow("Environment", e.action.LeftNodeId())

	if imgui.BeginV("Environment", nil, imgui.WindowFlagsNoMove) {
		imgui.DockBuilderGetNode(imgui.GetWindowDockID()).SetLocalFlags(imgui.DockNodeFlagsNoTabBar | imgui.DockNodeFlagsNoDocking | imgui.DockNodeFlagsNoDockingSplitMe)

		if e.action.LoadedEnvironment() == nil {
			imgui.Text("No Environment Loaded")
		} else {
			e.showControls()
			e.showTree()
		}
	}
	imgui.End()
}

func (e *Environment) showControls() {
	if imgui.Button("-") {

	}
	imgui.SameLine()
	imgui.SetNextItemWidth(-1)
	if imgui.InputTextWithHint("##filter", "Filter", &e.filter) {
		e.doFilter()
	}
	imgui.Separator()
}

func (e *Environment) showTree() {
	if imgui.BeginChild("tree") {
		if len(e.filter) == 0 {
			e.showTypeBranch("/area")
			e.showTypeBranch("/turf")
			e.showTypeBranch("/obj")
			e.showTypeBranch("/mob")
		} else {
			e.showFilteredNodes()
		}
		imgui.EndChild()
	}
}

func (e *Environment) showFilteredNodes() {
	var clipper imgui.ListClipper
	clipper.Begin(len(e.filteredTreeNodes))
	for clipper.Step() {
		for i := clipper.DisplayStart; i < clipper.DisplayEnd; i++ {
			node := e.filteredTreeNodes[i]
			imgui.TreeNodeV(node.orig.Type, imgui.TreeNodeFlagsLeaf|imgui.TreeNodeFlagsNoTreePushOnOpen)
		}
	}
}

func (e *Environment) showTypeBranch(t string) {
	if atom := e.action.LoadedEnvironment().Items[t]; atom != nil {
		e.showBranch0(atom)
	}
}

func (e *Environment) showBranch0(item *byond.DmeItem) {
	node := e.treeNode(item)

	if len(item.Children) == 0 {
		imgui.TreeNodeV(node.name, imgui.TreeNodeFlagsLeaf|imgui.TreeNodeFlagsNoTreePushOnOpen)
	} else {
		if imgui.TreeNodeV(node.name, imgui.TreeNodeFlagsOpenOnArrow|imgui.TreeNodeFlagsOpenOnDoubleClick) {
			for _, childType := range item.Children {
				e.showBranch0(e.action.LoadedEnvironment().Items[childType])
			}
			imgui.TreePop()
		}
	}
}

func (e *Environment) doFilter() {
	e.filteredTreeNodes = nil

	if len(e.filter) == 0 {
		return
	}

	e.filterTypeBranch("/area")
	e.filterTypeBranch("/turf")
	e.filterTypeBranch("/obj")
	e.filterTypeBranch("/mob")
}

func (e *Environment) filterTypeBranch(t string) {
	if atom := e.action.LoadedEnvironment().Items[t]; atom != nil {
		e.filterBranch0(atom)
	}
}

func (e *Environment) filterBranch0(item *byond.DmeItem) {
	if strings.Contains(item.Type, e.filter) {
		e.filteredTreeNodes = append(e.filteredTreeNodes, e.treeNode(item))
	}

	for _, childType := range item.Children {
		e.filterBranch0(e.action.LoadedEnvironment().Items[childType])
	}
}

type treeNode struct {
	name string
	orig *byond.DmeItem
}

func (e *Environment) treeNode(item *byond.DmeItem) *treeNode {
	if node := e.treeNodes[item.Type]; node != nil {
		return node
	}

	node := &treeNode{
		name: item.Type[strings.LastIndex(item.Type, "/")+1:],
		orig: item,
	}

	e.treeNodes[item.Type] = node
	return node
}
