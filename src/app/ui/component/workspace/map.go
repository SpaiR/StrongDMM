package workspace

import (
	"fmt"
	"log"

	"sdmm/app/ui/component/workspace/pmap"
	"sdmm/dm/dmmap"
)

type MapAction interface {
	pmap.Action

	AppIsCommandStackModified(id string) bool
	AppForceBalanceCommandStack(id string)
}

type Map struct {
	base

	action MapAction

	PaneMap *pmap.PaneMap
}

func NewMap(action MapAction, dmm *dmmap.Dmm) *Map {
	ws := &Map{
		PaneMap: pmap.New(action, dmm),
	}

	ws.Workspace = ws
	ws.action = action

	return ws
}

func (m *Map) Save() {
	log.Println("[workspace] saving map workspace:", m.Id())
	m.PaneMap.Dmm.Save()
	m.action.AppForceBalanceCommandStack(m.Id())
}

func (m *Map) Id() string {
	return m.PaneMap.Dmm.Path.Absolute
}

func (m *Map) Name() string {
	visibleName := m.PaneMap.Dmm.Name
	if m.action.AppIsCommandStackModified(m.Id()) {
		visibleName += " *"
	}
	return fmt.Sprint(visibleName, "###workspace_map_", m.PaneMap.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	m.PaneMap.Process()
}

func (m *Map) Tooltip() string {
	return m.PaneMap.Dmm.Path.Readable
}

func (m *Map) Dispose() {
	m.PaneMap.Dispose()
	log.Println("[workspace] map workspace disposed:", m.Name())
}

func (m *Map) Border() bool {
	return false
}
