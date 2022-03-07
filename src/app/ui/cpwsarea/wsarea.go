package cpwsarea

import (
	"fmt"
	"log"
	"sdmm/app/config"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/wschangelog"
	"sdmm/app/ui/cpwsarea/wsempty"
	"sdmm/app/ui/cpwsarea/wsmap"
	"sdmm/app/ui/cpwsarea/wsnewmap"
	"sdmm/app/ui/cpwsarea/wsprefs"
	"sdmm/app/ui/dialog"
	"sdmm/rsc"
	"sdmm/util"

	"sdmm/app/command"
	"sdmm/dmapi/dmmap"
)

type App interface {
	wsempty.App
	wsmap.App
	wsprefs.App

	DoOpenMapByPathV(mapPath string, workspace *workspace.Workspace)
	DoOpenMapByPath(path string)

	DoClose()

	OnWorkspaceSwitched()
	CommandStorage() *command.Storage
	IsLayoutReset() bool

	PointSize() float32

	ConfigRegister(config.Config)
	ConfigFind(name string) config.Config
	ConfigSaveV(config.Config)
}

type WsArea struct {
	app App

	activeWs *workspace.Workspace

	activeWsContentId string

	workspaces []*workspace.Workspace
}

func (w *WsArea) Init(app App) {
	w.app = app
	w.loadConfig()
	w.AddEmptyWorkspace()
	if w.isChangelogHashModified() {
		w.OpenChangelog()
	}
}

func (w *WsArea) Free() {
	w.closeWorkspaces(w.findMapWorkspaces())
	w.closeWorkspaces(w.findNewMapWorkspaces()) // close new map creation as well
	log.Println("[cpwsarea] workspace area free")
}

func (w *WsArea) OpenPreferences(prefsView wsprefs.Prefs) {
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsprefs.WsPrefs); ok {
			ws.SetTriggerFocus(true)
			return
		}
	}
	w.addWorkspace(workspace.New(wsprefs.New(w.app, prefsView)))
}

func (w *WsArea) OpenChangelog() {
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wschangelog.WsChangelog); ok {
			ws.SetTriggerFocus(true)
			return
		}
	}
	w.addWorkspace(workspace.New(wschangelog.New()))
}

func (w *WsArea) OpenNewMap() {
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsnewmap.WsNewMap); ok {
			ws.SetTriggerFocus(true)
			return
		}
	}

	wsCnt := wsnewmap.New(w.app)
	ws := workspace.New(wsCnt)
	wsCnt.SetOnOpenMapByPath(w.openMapByPath(ws))

	w.addWorkspace(ws)
}

func (w *WsArea) OpenMap(dmm *dmmap.Dmm, ws *workspace.Workspace) bool {
	if wsMap, ok := w.findMapWorkspace(dmm.Path); ok {
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

func (w *WsArea) Close() {
	if w.activeWs != nil {
		w.closeWorkspaceGently(w.activeWs)
	}
}

func (w *WsArea) CloseAll() {
	w.closeWorkspacesGently(w.workspaces)
}

func (w *WsArea) CloseAllMaps(callback func(closed bool)) {
	log.Println("[cpwsarea] closing all maps...")
	w.closeWorkspacesGentlyV(w.findMapWorkspaces(), callback)
}

func (w *WsArea) CloseAllNewMaps() {
	log.Println("[cpwsarea] closing all new maps...")
	w.closeWorkspaces(w.findNewMapWorkspaces())
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

func (w *WsArea) addWorkspace(ws *workspace.Workspace) {
	w.addWorkspaceV(ws, len(w.workspaces))
}

func (w *WsArea) addWorkspaceV(ws *workspace.Workspace, idx int) {
	w.workspaces = append(w.workspaces[:idx], append([]*workspace.Workspace{ws}, w.workspaces[idx:]...)...)
	log.Printf("[cpwsarea] workspace opened in index [%d]: %s", idx, ws.Name())
}

func (w *WsArea) closeWorkspacesGently(wsToClose []*workspace.Workspace) {
	w.closeWorkspacesGentlyV(wsToClose, nil)
}

func (w *WsArea) closeWorkspacesGentlyV(wsToClose []*workspace.Workspace, callback func(closed bool)) {
	var unsavedWorkspaces []*workspace.Workspace
	for _, ws := range wsToClose {
		if w.isWorkspaceUnsaved(ws) {
			unsavedWorkspaces = append(unsavedWorkspaces, ws)
		}
	}

	if len(unsavedWorkspaces) == 0 {
		w.closeWorkspaces(wsToClose)
		if callback != nil {
			callback(true)
		}
		return
	}

	var dType dialog.TypeConfirmation
	if len(unsavedWorkspaces) > 1 {
		dType = makeSaveMultipleWorkspacesDialogType(unsavedWorkspaces)
	} else {
		dType = makeSaveSingleWorkspaceDialogType(unsavedWorkspaces[0])
	}

	dType.ActionYes = func() {
		for _, ws := range unsavedWorkspaces {
			ws.Save()
		}
		w.closeWorkspaces(wsToClose)
		if callback != nil {
			callback(true)
		}
	}
	dType.ActionNo = func() {
		for _, ws := range unsavedWorkspaces {
			w.app.CommandStorage().Balance(ws.CommandStackId())
		}
		w.closeWorkspaces(wsToClose)
		if callback != nil {
			callback(true)
		}
	}
	dType.ActionCancel = func() {
		if callback != nil {
			callback(false)
		}
	}

	dialog.Open(dType)
}

func (w *WsArea) closeWorkspaces(wsToClose []*workspace.Workspace) {
	workspaces := make([]*workspace.Workspace, len(wsToClose))
	copy(workspaces, wsToClose)
	for _, ws := range workspaces {
		w.closeWorkspace(ws)
	}
}

func (w *WsArea) closeWorkspaceGently(ws *workspace.Workspace) {
	w.closeWorkspaceGentlyV(ws, nil)
}

func (w *WsArea) closeWorkspaceGentlyV(ws *workspace.Workspace, callback func(closed bool)) {
	if !w.isWorkspaceUnsaved(ws) {
		w.closeWorkspace(ws)
		return
	}

	dType := makeSaveSingleWorkspaceDialogType(ws)
	dType.ActionYes = func() {
		ws.Save()
		w.closeWorkspace(ws)
		if callback != nil {
			callback(true)
		}
	}
	dType.ActionNo = func() {
		w.app.CommandStorage().Balance(ws.CommandStackId())
		w.closeWorkspace(ws)
		if callback != nil {
			callback(true)
		}
	}
	dType.ActionCancel = func() {
		if callback != nil {
			callback(false)
		}
	}

	dialog.Open(dType)
}

func makeSaveSingleWorkspaceDialogType(ws *workspace.Workspace) dialog.TypeConfirmation {
	return dialog.TypeConfirmation{
		Title:    "Save Workspace?",
		Question: fmt.Sprintf("Workspace \"%s\" has been modified. Save changes?", ws.Title()),
	}
}

func makeSaveMultipleWorkspacesDialogType(workspaces []*workspace.Workspace) dialog.TypeConfirmation {
	var wsNames string
	for _, ws := range workspaces {
		wsNames += " - " + ws.Title() + "\n"
	}
	return dialog.TypeConfirmation{
		Title:    "Save All Workspaces?",
		Question: fmt.Sprintf("You have multiple unsaved workspaces:\n%sSave changes?", wsNames),
	}
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

func (w *WsArea) AddEmptyWorkspaceIfNone() {
	if w.findEmptyWorkspaceIdx() == -1 {
		w.AddEmptyWorkspace()
	}
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

func (w *WsArea) findMapWorkspace(path dmmap.DmmPath) (*workspace.Workspace, bool) {
	for _, ws := range w.workspaces {
		if wsCnt, ok := ws.Content().(*wsmap.WsMap); ok {
			if wsCnt.Map().Dmm().Path == path {
				return ws, true
			}
		}
	}
	return nil, false
}

func (w *WsArea) findMapWorkspaces() []*workspace.Workspace {
	var workspaces []*workspace.Workspace
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsmap.WsMap); ok {
			workspaces = append(workspaces, ws)
		}
	}
	return workspaces
}

func (w *WsArea) findNewMapWorkspaces() []*workspace.Workspace {
	var workspaces []*workspace.Workspace
	for _, ws := range w.workspaces {
		if _, ok := ws.Content().(*wsnewmap.WsNewMap); ok {
			workspaces = append(workspaces, ws)
		}
	}
	return workspaces
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

func (w *WsArea) isWorkspaceUnsaved(ws *workspace.Workspace) bool {
	return w.app.CommandStorage().IsModified(ws.CommandStackId())
}

func (w *WsArea) isChangelogHashModified() bool {
	if hash := util.Djb2(rsc.Changelog); hash != w.config().LastChangelogHash {
		w.config().LastChangelogHash = hash
		w.app.ConfigSaveV(w.config())
		return true
	}
	return false
}
