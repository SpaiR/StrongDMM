package cpprefabs

import "github.com/SpaiR/imgui-go"

func (p *Prefabs) showContextMenu(node *prefabNode) {
	if imgui.BeginPopupContextWindowV("context_menu", imgui.PopupFlagsMouseButtonRight) {
		imgui.MenuItem("Find Prefab on Map")
		imgui.MenuItem("Find Object on Map")
		imgui.Separator()
		imgui.MenuItem("New")
		imgui.MenuItem("Delete")
		imgui.Separator()
		imgui.MenuItem("Prefabs from icon states")
		imgui.MenuItem("Prefabs from directions")
		imgui.EndPopup()
	}
}
