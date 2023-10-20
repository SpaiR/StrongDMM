package cpenvironment

import (
	"fmt"

	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/imguiext/icon"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/platform"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
)

func (e *Environment) showNodeMenu(n *treeNode) {
	if imgui.BeginPopupContextItemV(fmt.Sprint("environment_node_menu_", n.orig.Path), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Find on Map", e.doFindOnMap(n)).
				Icon(icon.Search).
				Enabled(e.app.HasActiveMap()),
			w.MenuItem("Copy Type", e.doCopyType(n)).
				Icon(icon.ContentCopy),
		}.Build()
		imgui.EndPopup()
	}
}

func (e *Environment) doFindOnMap(n *treeNode) func() {
	return func() {
		prefab := dmmap.PrefabStorage.Initial(n.orig.Path)
		log.Print("do find object on map:", prefab.Path())
		e.app.ShowLayout(lnode.NameSearch, true)
		e.app.DoSearchPrefabByPath(prefab.Path())
	}
}

func (e *Environment) doCopyType(n *treeNode) func() {
	return func() {
		log.Print("do copy type:", n.orig.Path)
		platform.SetClipboard(n.orig.Path)
	}
}
