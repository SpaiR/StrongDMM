package wsmap

import (
	"fmt"
	"log"

	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmsave"
)

type App interface {
	pmap.App

	CommandStorage() *command.Storage
}

type WsMap struct {
	workspace.Base

	app App

	paneMap *pmap.PaneMap
}

func New(app App, dmm *dmmap.Dmm) *WsMap {
	ws := &WsMap{
		paneMap: pmap.New(app, dmm),
	}

	ws.Workspace = ws
	ws.app = app

	return ws
}

func (ws *WsMap) Map() *pmap.PaneMap {
	return ws.paneMap
}

func (ws *WsMap) Save() {
	log.Println("[wsmap] saving map workspace:", ws.CommandStackId())
	dmmsave.Save(ws.paneMap.Dmm())
	ws.app.CommandStorage().ForceBalance(ws.CommandStackId())
}

func (ws *WsMap) CommandStackId() string {
	return ws.paneMap.Dmm().Path.Absolute
}

func (ws *WsMap) Name() string {
	visibleName := ws.paneMap.Dmm().Name
	if ws.app.CommandStorage().IsModified(ws.CommandStackId()) {
		visibleName = "* " + visibleName
	}
	return fmt.Sprint(visibleName, "###workspace_map_", ws.paneMap.Dmm().Path.Absolute)
}

func (ws *WsMap) NameReadable() string {
	return ws.paneMap.Dmm().Name
}

func (ws *WsMap) PreProcess() {
	ws.paneMap.SetShortcutsVisible(false)
}

func (ws *WsMap) ShowContent() {
	ws.paneMap.Process()
}

func (ws *WsMap) Dispose() {
	ws.paneMap.Dispose()
	log.Println("[wsmap] map workspace disposed:", ws.Name())
}

func (ws *WsMap) Border() bool {
	return false
}

func (ws *WsMap) Focused() bool {
	return ws.paneMap.Focused()
}

func (ws *WsMap) OnActivate() {
	ws.paneMap.OnActivate()
}

func (ws *WsMap) OnDeactivate() {
	ws.paneMap.OnDeactivate()
}
