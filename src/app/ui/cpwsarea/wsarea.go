package cpwsarea

import (
	"log"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/wsempty"
	"sdmm/app/ui/cpwsarea/wsmap"
	"sdmm/app/ui/cpwsarea/wsprefs"

	"sdmm/app/command"
	"sdmm/dmapi/dmmap"
)

type App interface {
	wsempty.App
	wsmap.App

	DoOpenMapByPathV(mapPath string, workspace *workspace.Workspace)

	OnWorkspaceSwitched()
	CommandStorage() *command.Storage
	IsLayoutReset() bool
}

type WsArea struct {
	app App

	focused bool

	activeWs *workspace.Workspace

	activeWsContentId string

	workspaces []*workspace.Workspace
}

func (w *WsArea) Init(app App) {
	w.app = app
	w.AddEmptyWorkspace()
}

func (w *WsArea) Free() {
	w.closeAllMaps()
	log.Println("[cpwsarea] workspace area free")
}

func (w *WsArea) OpenPreferences(prefsView wsprefs.Prefs) {
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsprefs.WsPrefs); ok {
			ws.SetTriggerFocus(true)
			return
		}
	}
	w.addWorkspace(workspace.New(wsprefs.New(prefsView)))
}

func (w *WsArea) OpenMap(dmm *dmmap.Dmm, ws *workspace.Workspace) bool {
	if wsMap, ok := w.mapWorkspace(dmm.Path); ok {
		wsMap.SetTriggerFocus(true)
		return false
	}

	wsCnt := wsmap.New(w.app, dmm)
	if ws != nil {
		ws.SetContent(wsCnt)
	} else {
		w.addWorkspace(workspace.New(wsCnt))
	}

	return true
}

func (w *WsArea) WorkspaceTitle() string {
	if w.activeWs != nil {
		return w.activeWs.Title()
	}
	return ""
}

func (w *WsArea) ActiveWorkspace() *workspace.Workspace {
	return w.activeWs
}

func (w *WsArea) closeAllMaps() {
	workspaces := make([]*workspace.Workspace, len(w.workspaces))
	copy(workspaces, w.workspaces)
	for _, ws := range workspaces {
		if _, ok := ws.Content().(*wsmap.WsMap); ok {
			w.closeWorkspace(ws)
		}
	}
	if w.findEmptyWorkspaceIdx() == -1 {
		w.AddEmptyWorkspace()
	}
}

func (w *WsArea) mapWorkspace(path dmmap.DmmPath) (*workspace.Workspace, bool) {
	for _, ws := range w.workspaces {
		if wsCnt, ok := ws.Content().(*wsmap.WsMap); ok {
			if wsCnt.Map().Dmm().Path == path {
				return ws, true
			}
		}
	}
	return nil, false
}

func (w *WsArea) addWorkspace(ws *workspace.Workspace) {
	w.addWorkspaceV(ws, len(w.workspaces))
}

func (w *WsArea) addWorkspaceV(ws *workspace.Workspace, idx int) {
	w.workspaces = append(w.workspaces[:idx], append([]*workspace.Workspace{ws}, w.workspaces[idx:]...)...)
	log.Printf("[cpwsarea] workspace opened in index [%d]: %s", idx, ws.Name())
}

func (w *WsArea) closeWorkspace(ws *workspace.Workspace) {
	if idx := w.findWorkspaceIdx(ws); idx != -1 {
		w.closeWorkspaceByIdx(idx)
	}
}

func (w *WsArea) closeWorkspaceByIdx(idx int) {
	ws := w.workspaces[idx]
	w.workspaces = append(w.workspaces[:idx], w.workspaces[idx+1:]...)
	ws.Dispose()
	log.Printf("[cpwsarea] workspace closed in idx [%d]: %s", idx, ws.Name())
	w.app.CommandStorage().DisposeStack(ws.CommandStackId())
}

func (w *WsArea) AddEmptyWorkspace() {
	wsCnt := wsempty.New(w.app)
	ws := workspace.New(wsCnt)
	wsCnt.SetOnOpenMapByPath(w.openMapByPath(ws))
	w.addWorkspace(ws)
}

func (w *WsArea) openMapByPath(ws *workspace.Workspace) func(string) {
	return func(mapPath string) {
		w.app.DoOpenMapByPathV(mapPath, ws)
	}
}

func (w *WsArea) findWorkspaceIdx(ws *workspace.Workspace) int {
	for idx, lws := range w.workspaces {
		if lws == ws {
			return idx
		}
	}
	return -1
}

func (w *WsArea) findEmptyWorkspaceIdx() int {
	for idx, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsempty.WsEmpty); ok {
			return idx
		}
	}
	return -1
}

func (w *WsArea) switchActiveWorkspace(activeWs *workspace.Workspace) {
	if w.activeWs != activeWs || (activeWs != nil && w.activeWsContentId != activeWs.Content().Id()) {
		log.Println("[wsarea] switch active workspace:", activeWs)

		if activeWs != nil {
			log.Println("[wsarea] active workspace content:", activeWs.Content().Id())
		}

		if w.activeWs != nil {
			w.activeWs.OnFocusChange(false)
		}

		w.activeWs = activeWs

		if activeWs != nil {
			w.activeWsContentId = activeWs.Content().Id()
		} else {
			w.activeWsContentId = ""
		}

		if w.activeWs != nil {
			w.activeWs.OnFocusChange(true)
		}

		w.app.OnWorkspaceSwitched()

		if activeWs == nil {
			w.app.CommandStorage().SetStack(command.NullSpaceStackId)
		} else {
			w.app.CommandStorage().SetStack(w.activeWs.CommandStackId())
		}
	}
}
