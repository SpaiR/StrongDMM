package cpprefabs

import (
	"sdmm/internal/app/ui/component"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/editor"
	"sdmm/internal/app/window"

	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"

	"github.com/rs/zerolog/log"
)

type App interface {
	DoSelectPrefab(*dmmprefab.Prefab)
	DoEditPrefab(prefab *dmmprefab.Prefab)
	DoSearchPrefab(prefabId uint64)
	DoSearchPrefabByPath(path string)
	HasActiveMap() bool
	ShowLayout(name string, focus bool)
	CurrentEditor() *editor.Editor
}

type Prefabs struct {
	component.Component

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
	p.selectedId = dmmprefab.IdNone
}

func (p *Prefabs) Select(prefab *dmmprefab.Prefab) {
	p.nodes = newPrefabNodes(dmmap.PrefabStorage.GetAllByPath(prefab.Path()))

	// A special case for a "staged" prefab.
	if prefab.Id() == dmmprefab.IdStage {
		p.nodes = append(p.nodes, newPrefabNodeV(prefab, "[STAGED]"))
	}

	p.selectedId = prefab.Id()
	p.tmpDoScrollToPrefab = true
	log.Print("selected prefab id:", p.selectedId)
}

func (p *Prefabs) Sync() {
	if p.selectedId != dmmprefab.IdNone {
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
	return 32 * window.PointSize()
}
