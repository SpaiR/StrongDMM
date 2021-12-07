package cpprefabs

import (
	"fmt"
	"log"
	"strconv"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dmmap"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
	"sdmm/platform"
)

func (p *Prefabs) showContextMenu(node *prefabNode) {
	if imgui.BeginPopupContextItemV(fmt.Sprintf("context_menu_%d", node.orig.Id()), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Copy ID", p.doCopyId(node)).
				Icon(imguiext.IconFaCopy),
			w.MenuItem("Copy Type", p.doCopyType(node)).
				Icon(imguiext.IconFaCopy),
			w.Separator(),
			w.MenuItem("Find on Map", p.doFindOnMap(node)).
				Icon(imguiext.IconFaSearch).
				Enabled(p.app.HasActiveMap()),
			w.Separator(),
			w.MenuItem("New", p.doNew(node)).
				Icon(imguiext.IconFaPlusSquare),
			w.MenuItem("Delete", p.doDelete(node)).
				Icon(imguiext.IconFaEraser),
			w.Separator(),
			w.MenuItem("Generate icon states", nil).
				IconEmpty(),
			w.MenuItem("Generate directions", nil).
				IconEmpty(),
		}.Build()
		imgui.EndPopup()
	}
}

func (*Prefabs) doCopyId(node *prefabNode) func() {
	return func() {
		log.Println("[cpprefabs] do copy prefab id:", node.orig.Id())
		platform.SetClipboard(strconv.FormatUint(node.orig.Id(), 10))
	}
}

func (*Prefabs) doCopyType(node *prefabNode) func() {
	return func() {
		log.Println("[cpprefabs] do copy prefab type:", node.orig.Path())
		platform.SetClipboard(node.orig.Path())
	}
}

func (p *Prefabs) doFindOnMap(node *prefabNode) func() {
	return func() {
		log.Println("[cpprefabs] do find prefab on map:", node.orig.Id())
		p.app.ShowLayout(lnode.NameSearch, true)
		p.app.DoSearchPrefab(node.orig.Id())
	}
}

func (p *Prefabs) doNew(node *prefabNode) func() {
	return func() {
		log.Println("[cpprefabs] do new prefab:", node.orig.Id())
		prefab := node.orig.Stage()
		p.app.DoSelectPrefab(&prefab)
		p.app.DoEditPrefab(&prefab)
	}
}

func (p *Prefabs) doDelete(node *prefabNode) func() {
	return func() {
		log.Println("[cpprefabs] do delete prefab:", node.orig.Id())
		p.app.CurrentEditor().DeleteInstancesByPrefab(node.orig)

		// Delete the prefab from the prefabs list if it's not an initial one (which is always the first in the list).
		if node.orig.Id() != p.nodes[0].orig.Id() {
			dmmap.PrefabStorage.Delete(node.orig)
			p.selectedId = p.nodes[0].orig.Id()
			p.Update()
		}
	}
}
