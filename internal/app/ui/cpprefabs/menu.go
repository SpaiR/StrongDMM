package cpprefabs

import (
	"fmt"
	"strconv"

	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmvars"
	"sdmm/internal/imguiext/icon"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/platform"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
)

func (p *Prefabs) showContextMenu(node *prefabNode) {
	if imgui.BeginPopupContextItemV(fmt.Sprintf("context_menu_%d", node.orig.Id()), imgui.PopupFlagsMouseButtonRight) {
		w.Layout{
			w.MenuItem("Search by Type", p.doSearchByTypeOnMap(node)).
				Icon(icon.Search).
				Enabled(p.app.HasActiveMap()),
			w.MenuItem("Search by Prefab ID", p.doSearchByIdOnMap(node)).
				Icon(icon.Search).
				Enabled(p.app.HasActiveMap()),
			w.Separator(),
			w.MenuItem("New", p.doNew(node)).
				Icon(icon.AddBox),
			w.MenuItem("Delete", p.doDelete(node)).
				Icon(icon.Eraser),
			w.Separator(),
			w.MenuItem("Generate icon states", p.doGenerateIconStates(node)).
				IconEmpty(),
			w.MenuItem("Generate directions", p.doGenerateDirections(node)).
				IconEmpty(),
			w.Separator(),
			w.MenuItem("Copy Type", p.doCopyType(node)).
				Icon(icon.ContentCopy),
			w.MenuItem("Copy Prefab ID", p.doCopyId(node)).
				Icon(icon.ContentCopy),
		}.Build()
		imgui.EndPopup()
	}
}

func (*Prefabs) doCopyId(node *prefabNode) func() {
	return func() {
		log.Print("do copy prefab id:", node.orig.Id())
		platform.SetClipboard(strconv.FormatUint(node.orig.Id(), 10))
	}
}

func (*Prefabs) doCopyType(node *prefabNode) func() {
	return func() {
		log.Print("do copy prefab type:", node.orig.Path())
		platform.SetClipboard(node.orig.Path())
	}
}

func (p *Prefabs) doSearchByTypeOnMap(node *prefabNode) func() {
	return func() {
		log.Print("do search prefab by type on map:", node.orig.Path())
		p.app.ShowLayout(lnode.NameSearch, true)
		p.app.DoSearchPrefabByPath(node.orig.Path())
	}
}

func (p *Prefabs) doSearchByIdOnMap(node *prefabNode) func() {
	return func() {
		log.Print("do search prefab by ID on map:", node.orig.Id())
		p.app.ShowLayout(lnode.NameSearch, true)
		p.app.DoSearchPrefab(node.orig.Id())
	}
}

func (p *Prefabs) doNew(node *prefabNode) func() {
	return func() {
		log.Print("do new prefab:", node.orig.Id())
		prefab := node.orig.Stage()
		p.app.DoSelectPrefab(&prefab)
		p.app.DoEditPrefab(&prefab)
	}
}

func (p *Prefabs) doDelete(node *prefabNode) func() {
	return func() {
		log.Print("do delete prefab:", node.orig.Id())
		p.app.CurrentEditor().InstancesDeleteByPrefab(node.orig)
		p.app.CurrentEditor().CommitChanges("Delete Instances")

		// Delete the prefab from the prefabs list if it's not an initial one (which is always the first in the list).
		if node.orig.Id() != p.nodes[0].orig.Id() {
			dmmap.PrefabStorage.Delete(node.orig)
			p.selectedId = p.nodes[0].orig.Id()
			p.Sync()
		}
	}
}

func (p *Prefabs) doGenerateIconStates(node *prefabNode) func() {
	return func() {
		log.Print("do generate prefabs from icon states:", node.orig.Id())

		dmi := node.sprite.Dmi()
		for name := range dmi.States {
			if node.orig.Vars().TextV("icon_state", "") == name {
				continue
			}
			vars := dmvars.Set(node.orig.Vars(), "icon_state", "\""+name+"\"")
			dmmap.PrefabStorage.Get(node.orig.Path(), vars)
		}

		p.Sync()
	}
}

func (p *Prefabs) doGenerateDirections(node *prefabNode) func() {
	return func() {
		log.Print("do generate prefabs from directions:", node.orig.Id())

		initialDir := node.orig.Vars().IntV("dir", dm.DirDefault)
		dmi := node.sprite.Dmi()
		state := dmi.States[node.orig.Vars().TextV("icon_state", "")]

		var dirs []int

		switch state.Dirs {
		case 4:
			dirs = append(dirs, []int{
				dm.DirNorth, dm.DirSouth, dm.DirEast, dm.DirWest}...,
			)
		case 8:
			dirs = append(dirs, []int{
				dm.DirNorth, dm.DirSouth, dm.DirEast, dm.DirWest,
				dm.DirNortheast, dm.DirNorthwest, dm.DirSoutheast, dm.DirSouthwest}...,
			)
		}

		for _, dir := range dirs {
			if dir == initialDir {
				continue
			}
			vars := dmvars.Set(node.orig.Vars(), "dir", strconv.Itoa(dir))
			dmmap.PrefabStorage.Get(node.orig.Path(), vars)
		}

		p.Sync()
	}
}
