package cpwsarea

import (
	"log"

	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/workspace/wsempty"
	"sdmm/app/ui/cpwsarea/workspace/wsmap"
	"sdmm/app/ui/cpwsarea/workspace/wsprefs"
	"sdmm/dm/dmmap"
)

type Action interface {
	wsempty.Action
	wsmap.Action

	AppUpdateTitle()
	AppSwitchCommandStack(id string)
	AppDisposeCommandStack(id string)
}

type WsArea struct {
	action Action

	activeWs workspace.Workspace

	workspaces []workspace.Workspace
}

func (w *WsArea) Init(action Action) {
	w.action = action
	w.addEmptyWorkspace()
}

func (w *WsArea) Free() {
	w.closeAllMaps()
	log.Println("[cpwsarea] workspace area free")
}

func (w *WsArea) OpenPreferences(prefsView wsprefs.Prefs) {
	for _, ws := range w.workspaces {
		if ws, ok := ws.(*wsprefs.WsPrefs); ok {
			ws.Select(true)
			return
		}
	}

	w.addWorkspace(wsprefs.New(prefsView))
}

func (w *WsArea) OpenMap(dmm *dmmap.Dmm, workspaceIdx int) {
	if ws, ok := w.mapWorkspace(dmm.Path); ok {
		ws.Select(true)
		return
	}

	ws := wsmap.New(w.action, dmm)
	if workspaceIdx != -1 {
		w.closeWorkspaceByIdx(workspaceIdx)
		w.addWorkspaceV(ws, workspaceIdx)
	} else {
		w.addWorkspace(ws)
	}
}

func (w *WsArea) WorkspaceTitle() string {
	if w.activeWs != nil {
		return w.activeWs.Tooltip()
	}
	return ""
}

func (w *WsArea) ActiveWorkspace() workspace.Workspace {
	return w.activeWs
}

func (w *WsArea) closeAllMaps() {
	workspaces := make([]workspace.Workspace, len(w.workspaces))
	copy(workspaces, w.workspaces)
	for _, ws := range workspaces {
		if _, ok := ws.(*wsmap.WsMap); ok {
			w.closeWorkspace(ws)
		}
	}
	if w.findEmptyWorkspaceIdx() == -1 {
		w.addEmptyWorkspace()
	}
}

func (w *WsArea) mapWorkspace(path dmmap.DmmPath) (*wsmap.WsMap, bool) {
	for _, ws := range w.workspaces {
		if ws, ok := ws.(*wsmap.WsMap); ok {
			if ws.PaneMap.Dmm().Path == path {
				return ws, true
			}
		}
	}
	return nil, false
}

func (w *WsArea) addWorkspace(ws workspace.Workspace) {
	w.addWorkspaceV(ws, len(w.workspaces))
}

func (w *WsArea) addWorkspaceV(ws workspace.Workspace, idx int) {
	w.workspaces = append(w.workspaces[:idx], append([]workspace.Workspace{ws}, w.workspaces[idx:]...)...)
	log.Printf("[cpwsarea] workspace opened in index [%d]: %s", idx, ws.Name())
}

func (w *WsArea) closeWorkspace(ws workspace.Workspace) {
	if idx := w.findWorkspaceIdx(ws); idx != -1 {
		w.closeWorkspaceByIdx(idx)
	}
}

func (w *WsArea) closeWorkspaceByIdx(idx int) {
	if ws := w.workspaces[idx]; ws.WantClose() {
		w.workspaces = append(w.workspaces[:idx], w.workspaces[idx+1:]...)
		ws.Dispose()
		log.Printf("[cpwsarea] workspace closed in idx [%d]: %s", idx, ws.Name())
		w.action.AppDisposeCommandStack(ws.CommandStackId())
	}
}

func (w *WsArea) addEmptyWorkspace() {
	w.addWorkspace(wsempty.New(w.action))
}

func (w *WsArea) findWorkspaceIdx(ws workspace.Workspace) int {
	for idx, lws := range w.workspaces {
		if lws == ws {
			return idx
		}
	}
	return -1
}

func (w *WsArea) findEmptyWorkspaceIdx() int {
	for idx, ws := range w.workspaces {
		if _, ok := ws.(*wsempty.WsEmpty); ok {
			return idx
		}
	}
	return -1
}

func (w *WsArea) switchActiveWorkspace(activeWs workspace.Workspace) {
	if w.activeWs != activeWs {
		w.activeWs = activeWs

		w.action.AppUpdateTitle()

		if activeWs == nil {
			w.action.AppSwitchCommandStack(command.NullSpaceStackId)
		} else {
			w.action.AppSwitchCommandStack(w.activeWs.CommandStackId())
		}
	}
}
