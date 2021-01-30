package ui

import (
	"strings"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/byond/dmenv"
	"github.com/SpaiR/strongdmm/internal/app/byond/dmicon"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
	w "github.com/SpaiR/strongdmm/pkg/widget"
)

type environmentAction interface {
	LeftNodeId() int
	LoadedEnvironment() *dmenv.Dme
	PointSize() float32
}

// Only 25 nodes can be loaded per one process tick.
// This helps to distribute performance load between process calls.
const newTreeNodesLimit = 25

type Environment struct {
	action environmentAction

	treeNodes         map[string]*treeNode
	filteredTreeNodes []*treeNode

	filter string

	tmpNewTreeNodesCount int
	tmpDoRepeatFilter    bool
	tmpDoCollapseAll     bool
}

func NewEnvironment(action environmentAction) *Environment {
	return &Environment{
		action:    action,
		treeNodes: make(map[string]*treeNode),
	}
}

func (e *Environment) Free() {
	e.treeNodes = make(map[string]*treeNode)
	e.filteredTreeNodes = nil
	e.filter = ""
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

func (e *Environment) Process() {
	imgui.DockBuilderDockWindow("Environment", e.action.LeftNodeId())

	if imgui.BeginV("Environment", nil, imgui.WindowFlagsNoMove) {
		imgui.DockBuilderGetNode(imgui.GetWindowDockID()).SetLocalFlags(imgui.DockNodeFlagsNoCloseButton | imgui.DockNodeFlagsNoDocking | imgui.DockNodeFlagsNoDockingSplitMe)

		if e.action.LoadedEnvironment() == nil {
			imgui.Text("No Environment Loaded")
		} else {
			e.process()
			e.showControls()
			e.showTree()
			e.postProcess()
		}
	}
	imgui.End()
}

func (e *Environment) showControls() {
	if imgui.Button("-") {
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
			e.showIcon(node)
			imgui.TreeNodeV(node.orig.Path, imgui.TreeNodeFlagsLeaf|imgui.TreeNodeFlagsNoTreePushOnOpen)
		}
	}
}

func (e *Environment) showTypeBranch(t string) {
	if atom := e.action.LoadedEnvironment().Objects[t]; atom != nil {
		e.showBranch0(atom)
	}
}

func (e *Environment) showBranch0(object *dmenv.Object) {
	node, ok := e.treeNode(object)
	if ok != true {
		return
	}

	e.showIcon(node)

	if len(object.DirectChildren) == 0 {
		imgui.TreeNodeV(node.name, imgui.TreeNodeFlagsLeaf|imgui.TreeNodeFlagsNoTreePushOnOpen)
	} else {
		if imgui.TreeNodeV(node.name, imgui.TreeNodeFlagsOpenOnArrow|imgui.TreeNodeFlagsOpenOnDoubleClick) {
			if e.tmpDoCollapseAll {
				imgui.StateStorage().SetAllInt(0)
			}
			for _, childType := range object.DirectChildren {
				e.showBranch0(e.action.LoadedEnvironment().Objects[childType])
			}
			imgui.TreePop()
		}
	}
}

func (e *Environment) showIcon(node *treeNode) {
	s := node.sprite
	iconSize := 16 * e.action.PointSize()
	w.Image(imgui.TextureID(s.Texture()), iconSize, iconSize).Uv(imgui.Vec2{X: s.U1, Y: s.V1}, imgui.Vec2{X: s.U2, Y: s.V2}).Build()
	imgui.SameLine()
}

func (e *Environment) doFilter() {
	e.filteredTreeNodes = nil

	if len(e.filter) == 0 {
		return
	}

	initialNewTreeNodesCount := e.tmpNewTreeNodesCount

	e.filterTypeBranch("/area")
	e.filterTypeBranch("/turf")
	e.filterTypeBranch("/obj")
	e.filterTypeBranch("/mob")

	e.tmpDoRepeatFilter = initialNewTreeNodesCount != e.tmpNewTreeNodesCount
}

func (e *Environment) filterTypeBranch(t string) {
	if atom := e.action.LoadedEnvironment().Objects[t]; atom != nil {
		e.filterBranch0(atom)
	}
}

func (e *Environment) filterBranch0(object *dmenv.Object) {
	if strings.Contains(object.Path, e.filter) {
		if node, ok := e.treeNode(object); ok == true {
			e.filteredTreeNodes = append(e.filteredTreeNodes, node)
		}
	}

	for _, childType := range object.DirectChildren {
		e.filterBranch0(e.action.LoadedEnvironment().Objects[childType])
	}
}

type treeNode struct {
	name   string
	orig   *dmenv.Object
	sprite *dmicon.Sprite
}

func (e *Environment) treeNode(object *dmenv.Object) (*treeNode, bool) {
	if node, ok := e.treeNodes[object.Path]; ok {
		return node, true
	}

	if e.tmpNewTreeNodesCount >= newTreeNodesLimit {
		return nil, false
	}

	e.tmpNewTreeNodesCount += 1

	icon, _ := object.Vars.Text("icon")
	iconState, _ := object.Vars.Text("icon_state")

	node := &treeNode{
		name:   object.Path[strings.LastIndex(object.Path, "/")+1:],
		orig:   object,
		sprite: dmicon.GetSpriteOrPlaceholder(icon, iconState),
	}

	e.treeNodes[object.Path] = node
	return node, true
}
