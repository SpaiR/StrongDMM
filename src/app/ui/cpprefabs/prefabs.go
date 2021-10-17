package cpprefabs

import (
	"log"

	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
)

type App interface {
	PointSize() float32
	DoSelectPrefab(*dmmdata.Prefab)
}

type Prefabs struct {
	app App

	nodes      []*node
	selectedId uint64

	tmpDoScrollToPrefab bool
}

func (p *Prefabs) Init(app App) {
	p.app = app
}

func (p *Prefabs) Free() {
	p.nodes = nil
	p.selectedId = 0
}

func (p *Prefabs) Select(prefab *dmmdata.Prefab) {
	p.nodes = makeNodes(dmmap.PrefabStorage.GetAllByPath(prefab.Path()))
	p.selectedId = prefab.Id()
	p.tmpDoScrollToPrefab = true
	log.Println("[cpprefabs] selected prefab id:", p.selectedId)
}

func (p *Prefabs) Update() {
	if p.selectedId != 0 {
		if prefab, ok := dmmap.PrefabStorage.GetById(p.selectedId); ok {
			p.Select(prefab)
		}
	}
}

// SelectedPrefabId returns the id of the prefab currently selected in the Prefabs panel.
func (p *Prefabs) SelectedPrefabId() uint64 {
	return p.selectedId
}

func (p *Prefabs) doSelect(node *node) {
	p.app.DoSelectPrefab(node.orig)
	p.tmpDoScrollToPrefab = false // do not scroll panel when we're in panel itself
}

func (p *Prefabs) showContextMenu(node *node) {
	// TODO: prefab context menu
}

func (p *Prefabs) iconSize() float32 {
	return 32 * p.app.PointSize()
}

func (p *Prefabs) textIndent() float32 {
	return 36 * p.app.PointSize()
}

func (p *Prefabs) iconIndent() float32 {
	return 1 * p.app.PointSize()
}
