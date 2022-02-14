package cpenvironment

import (
	"fmt"
	"sdmm/imguiext/style"
	"strings"

	"github.com/SpaiR/imgui-go"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext/icon"
	w "sdmm/imguiext/widget"
)

func (e *Environment) Process() {
	e.shortcuts.SetVisibleIfFocused()

	if e.app.LoadedEnvironment() == nil {
		imgui.TextDisabled("No Environment Loaded")
	} else {
		e.process()
		e.showControls()
		e.showTree()
		e.postProcess()
	}
}

func (e *Environment) showControls() {
	w.Button(icon.FaMinus, e.doCollapseAll).
		Tooltip("Collapse All").
		Round(true).
		Build()
	imgui.SameLine()
	e.showTypesFilterButton()
	imgui.SameLine()
	w.InputTextWithHint("##filter", "Filter", &e.filter).
		ButtonClear().
		Width(-1).
		OnChange(e.doFilter).
		Build()
	imgui.Separator()
}

func (e *Environment) showTypesFilterButton() {
	var bStyle w.ButtonStyle

	if !e.typesFilterEnabled {
		bStyle = style.ButtonDefault{}
	} else {
		bStyle = style.ButtonGreen{}
	}

	w.Button(icon.FaEye, e.doToggleTypesFilter).
		Tooltip("Types Filter (F)").
		Round(true).
		Style(bStyle).
		Build()
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
			if e.showAttachment(node) {
				imgui.SameLine()
			}
			imgui.TreeNodeV(node.orig.Path, e.nodeFlags(node, true))
			e.doSelectOnClick(node)
			e.showNodeMenu(node)
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
	if !ok {
		return
	}

	if e.showAttachment(node) {
		imgui.SameLine()
	}

	if len(object.DirectChildren) == 0 {
		imgui.AlignTextToFramePadding()
		imgui.TreeNodeV(node.name, e.nodeFlags(node, true))
		e.doSelectOnClick(node)
		e.showNodeMenu(node)
		e.scrollToSelectedPath(node)
	} else {
		if e.isPartOfSelectedPath(node) {
			imgui.SetNextItemOpen(true, imgui.ConditionAlways)
		}
		imgui.AlignTextToFramePadding()
		if imgui.TreeNodeV(node.name, e.nodeFlags(node, false)) {
			e.doSelectOnClick(node)
			e.showNodeMenu(node)
			e.scrollToSelectedPath(node)

			if e.tmpDoCollapseAll {
				imgui.StateStorage().SetAllInt(0)
			}
			for _, childPath := range object.DirectChildren {
				e.showBranch0(e.app.LoadedEnvironment().Objects[childPath])
			}
			imgui.TreePop()
		} else {
			e.doSelectOnClick(node)
			e.showNodeMenu(node)
		}
	}
}

func (e *Environment) doSelectOnClick(node *treeNode) {
	if imgui.IsItemClicked() && e.selectedPath != node.orig.Path {
		e.app.DoSelectPrefabByPath(node.orig.Path)
		e.app.DoEditPrefabByPath(node.orig.Path)
		e.tmpDoSelectPath = false // we don't need to scroll tree when we select item from the tree itself
	}
}

func (e *Environment) nodeFlags(node *treeNode, leaf bool) imgui.TreeNodeFlags {
	flags := int(imgui.TreeNodeFlagsSpanAvailWidth)
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

func (e *Environment) showAttachment(node *treeNode) bool {
	if e.typesFilterEnabled {
		e.showVisibilityCheckbox(node)
		return true
	} else {
		e.showIcon(node)
		return false
	}
}

func (e *Environment) showVisibilityCheckbox(node *treeNode) {
	value := e.app.PathsFilter().IsVisiblePath(node.orig.Path)
	if imgui.Checkbox(fmt.Sprint("##node_visibility_", node.orig.Path), &value) {
		e.app.PathsFilter().TogglePath(node.orig.Path)
	}
}

func (e *Environment) showIcon(node *treeNode) {
	s := node.sprite
	iconSize := e.iconSize()
	w.Image(imgui.TextureID(s.Texture()), iconSize, iconSize).Uv(imgui.Vec2{X: s.U1, Y: s.V1}, imgui.Vec2{X: s.U2, Y: s.V2}).Build()
	imgui.SameLine()
}
