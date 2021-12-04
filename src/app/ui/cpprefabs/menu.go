package cpprefabs

import (
	"fmt"
	"log"
	"strconv"

	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
	"sdmm/platform"
)

func (p *Prefabs) showContextMenu(node *prefabNode) {
	if imgui.BeginPopupContextWindowV(fmt.Sprintf("context_menu_%d", node.orig.Id()), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Copy ID", p.doCopyId(node)).
				Icon(imguiext.IconFaCopy),
			w.MenuItem("Copy Type", p.doCopyType(node)).
				Icon(imguiext.IconFaCopy),
			w.Separator(),
			w.MenuItem("Find on Map", nil).
				Icon(imguiext.IconFaSearch).
				Enabled(p.app.HasActiveMap()),
			w.Separator(),
			w.MenuItem("New", nil).
				Icon(imguiext.IconFaPlusSquare),
			w.MenuItem("Delete", nil).
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
