package wsmap

import (
	"fmt"
	"log"

	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmsave"
)

type Action interface {
	pmap.Action

	AppIsCommandStackModified(id string) bool
	AppForceBalanceCommandStack(id string)
}

type WsMap struct {
	workspace.Base

	action Action

	PaneMap *pmap.PaneMap
}

func New(action Action, dmm *dmmap.Dmm) *WsMap {
	ws := &WsMap{
		PaneMap: pmap.New(action, dmm),
	}

	ws.Workspace = ws
	ws.action = action

	return ws
}

func (ws *WsMap) Save() {
	log.Println("[wsmap] saving map workspace:", ws.CommandStackId())
	dmmsave.Save(ws.PaneMap.Dmm())
	ws.action.AppForceBalanceCommandStack(ws.CommandStackId())
}

func (ws *WsMap) CommandStackId() string {
	return ws.PaneMap.Dmm().Path.Absolute
}

func (ws *WsMap) Name() string {
	visibleName := ws.PaneMap.Dmm().Name
	if ws.action.AppIsCommandStackModified(ws.CommandStackId()) {
		visibleName += " *"
	}
	return fmt.Sprint(visibleName, "###workspace_map_", ws.PaneMap.Dmm().Path.Absolute)
}

func (ws *WsMap) Process() {
	ws.PaneMap.Process()
}

func (ws *WsMap) Tooltip() string {
	return ws.PaneMap.Dmm().Path.Readable
}

func (ws *WsMap) Dispose() {
	ws.PaneMap.Dispose()
	log.Println("[wsmap] map workspace disposed:", ws.Name())
}

func (ws *WsMap) Border() bool {
	return false
}
