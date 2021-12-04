package cpprefabs

import (
	"github.com/SpaiR/imgui-go"
	w "sdmm/imguiext/widget"
)

func (p *Prefabs) showContextMenu(node *prefabNode) {
	if imgui.BeginPopupContextWindowV("context_menu", imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Find Prefab on Map", nil).
				Enabled(p.app.HasActiveMap()),
			w.MenuItem("Find Object on Map", nil).
				Enabled(p.app.HasActiveMap()),
			w.Separator(),
			w.MenuItem("New", nil),
			w.MenuItem("Delete", nil),
			w.Separator(),
			w.MenuItem("Prefabs from icon states", nil),
			w.MenuItem("Prefabs from directions", nil),
		}.Build()
		imgui.EndPopup()
	}
}
