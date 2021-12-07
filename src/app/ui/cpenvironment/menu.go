package cpenvironment

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dmmap"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

func (e *Environment) showNodeMenu(n *treeNode) {
	if imgui.BeginPopupContextItemV(fmt.Sprint("environment_node_menu_", n.orig.Path), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Find on Map", e.doFindOnMap(n)).
				Icon(imguiext.IconFaSearch).
				Enabled(e.app.HasActiveMap()),
		}.Build()
		imgui.EndPopup()
	}
}

func (e *Environment) doFindOnMap(n *treeNode) func() {
	return func() {
		prefab := dmmap.PrefabStorage.Initial(n.orig.Path)
		log.Println("[cpenvironment] do find object on map:", prefab.Path())
		e.app.ShowLayout(lnode.NameSearch, true)
		e.app.DoSearchPrefab(prefab.Id())
	}
}
