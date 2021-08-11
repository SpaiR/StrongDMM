package workspace

import (
	"fmt"
	"log"

	"github.com/SpaiR/strongdmm/app/ui/component/workspace/pmap"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
)

type MapAction interface {
	pmap.Action

	SetCommandStack(id string)
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

func (m *Map) Name() string {
	return fmt.Sprint(m.PaneMap.Dmm.Name, "##workspace_map_", m.PaneMap.Dmm.Path.Absolute)
}

func (m *Map) Process() {
	m.action.SetCommandStack(m.Name())
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
