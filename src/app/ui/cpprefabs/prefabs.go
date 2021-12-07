package cpprefabs

import (
	"log"

	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
)

type App interface {
	PointSize() float32
	DoSelectPrefab(*dmmprefab.Prefab)
	DoEditPrefab(prefab *dmmprefab.Prefab)
	DoSearchPrefab(prefabId uint64)
	HasActiveMap() bool
	ShowLayout(name string, focus bool)
	CurrentEditor() *pmap.Editor
}

type Prefabs struct {
	app App

	nodes      []*prefabNode
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

func (p *Prefabs) Select(prefab *dmmprefab.Prefab) {
	p.nodes = newPrefabNodes(dmmap.PrefabStorage.GetAllByPath(prefab.Path()))

	// A special case for a "staged" prefab.
	if prefab.Id() == dmmprefab.IdStage {
		p.nodes = append(p.nodes, newPrefabNodeV(prefab, "[STAGED]"))
	}

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

func (p *Prefabs) doSelect(node *prefabNode) {
	p.app.DoSelectPrefab(node.orig)
	p.app.DoEditPrefab(node.orig)
	p.tmpDoScrollToPrefab = false // do not scroll panel when we're in panel itself
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
