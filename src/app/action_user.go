package app

import (
	"log"

	"sdmm/app/prefs"
	"sdmm/app/render"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/app/ui/layout/lnode"
	"sdmm/app/window"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
	"sdmm/env"
	"sdmm/util/slice"

	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
)

/*
	File similar to action.go, but contains methods triggered by user. (ex. when button clicked)
	Such methods have a "Do" prefix, and they are logged excessively.
*/

func (a *app) DoNewWorkspace() {
	log.Println("[app] new workspace...")
	a.layout.WsArea.AddEmptyWorkspace()
}

// DoOpen opens environment, which user need to select in file dialog.
func (a *app) DoOpen() {
	a.DoOpenV(nil)
}

// DoOpenV opens environment, which user need to select in file dialog.
// Verbose version which handle opening in a specific workspace. Helpful to open maps/
func (a *app) DoOpenV(ws *workspace.Workspace) {
	log.Println("[app] selecting resource to load...")

	startDir := ""
	if a.HasLoadedEnvironment() {
		startDir = a.loadedEnvironment.RootDir
	}

	if file, err := dialog.
		File().
		Title("Open").
		Filter("Resource", "dme", "dmm").
		SetStartDir(startDir).
		Load(); err == nil {
		log.Println("[app] resource to load selected:", file)

		if a.HasLoadedEnvironment() {
			a.loadMap(file, ws)
		} else {
			a.DoLoadResourceV(file, ws)
		}
	}
}

// DoLoadResource opens environment by provided path.
func (a *app) DoLoadResource(path string) {
	a.DoLoadResourceV(path, nil)
}

// DoLoadResourceV opens environment by provided path.
// Verbose version which handle opening in a specific workspace. Helpful to open maps
func (a *app) DoLoadResourceV(path string, ws *workspace.Workspace) {
	log.Println("[app] load resource by path:", path)
	a.loadResourceV(path, ws)
}

// DoClearRecentEnvironments clears recently opened environments.
func (a *app) DoClearRecentEnvironments() {
	log.Println("[app] clear recent environments")
	a.projectConfig().ClearProjects()
}

// DoCloseEnvironment closes currently opened environment.
func (a *app) DoCloseEnvironment() {
	log.Println("[app] closing environment")
	a.closeEnvironment(func(closed bool) {
		if closed {
			a.freeEnvironmentResources()
			a.layout.WsArea.AddEmptyWorkspaceIfNone()
		}
	})
}

// DoCreateMap opens dialog window to create a new map file.
func (a *app) DoCreateMap() {
	log.Println("[app] opening create map...")
	a.layout.WsArea.OpenCreateMap()
}

// DoClearRecentMaps clears recently opened maps.
func (a *app) DoClearRecentMaps() {
	log.Println("[app] clear recent maps")
	a.projectConfig().ClearMaps()
}

// DoRemoveRecentMaps removes specific recent maps
func (a *app) DoRemoveRecentMaps(recentMaps []string) {
	log.Println("[app] do remove recent maps:", recentMaps)
	recentMaps = append(make([]string, 0, len(recentMaps)), recentMaps...)
	for _, recentMap := range recentMaps {
		a.projectConfig().RemoveMap(recentMap)
	}
}

// DoRemoveRecentEnvironment removes specific recent environment.
func (a *app) DoRemoveRecentEnvironment(envPath string) {
	log.Println("[app] remove recent environment:", envPath)
	a.projectConfig().RemoveEnvironment(envPath)
}

// DoRemoveRecentMap removes specific recent map.
func (a *app) DoRemoveRecentMap(mapPath string) {
	log.Println("[app] remove recent map:", mapPath)
	a.projectConfig().RemoveMap(mapPath)
}

// DoClose closes currently active workspace.
func (a *app) DoClose() {
	a.layout.WsArea.Close()
}

// DoCloseAll closes all opened workspaces.
func (a *app) DoCloseAll() {
	a.layout.WsArea.CloseAll()
}

// DoSave saves current active map.
func (a *app) DoSave() {
	log.Println("[app] do save")
	if ws, ok := a.activeWsMap(); ok {
		ws.Save()
	}
}

// DoSaveAll saves all active maps.
func (a *app) DoSaveAll() {
	log.Println("[app] do save all")
	for _, ws := range a.layout.WsArea.MapWorkspaces() {
		ws.Save()
	}
}

// DoOpenPreferences opens preferences tab.
func (a *app) DoOpenPreferences() {
	log.Println("[app] open preferences")
	a.layout.OpenPreferences(prefs.Make(a, &a.preferencesConfig().Prefs))
}

// DoSelectPrefab globally selects provided prefab in the app.
func (a *app) DoSelectPrefab(prefab *dmmprefab.Prefab) {
	log.Printf("[app] select prefab: path=[%s], id=[%d]", prefab.Path(), prefab.Id())
	a.layout.Environment.SelectPath(prefab.Path())
	a.layout.Prefabs.Select(prefab)
}

// DoSelectPrefabByPath globally selects a prefab with provided type path.
func (a *app) DoSelectPrefabByPath(path string) {
	log.Println("[app] select prefab by path:", path)
	a.DoSelectPrefab(dmmap.PrefabStorage.Initial(path))
}

// DoEditInstance enables an editing for the provided instance.
func (a *app) DoEditInstance(instance *dmminstance.Instance) {
	log.Println("[app] edit instance:", instance.Id())
	a.layout.VarEditor.EditInstance(instance)
}

// DoEditPrefab enables an editing for the provided prefab.
func (a *app) DoEditPrefab(prefab *dmmprefab.Prefab) {
	log.Println("[app] edit prefab:", prefab.Id())
	a.layout.VarEditor.EditPrefab(prefab)
}

// DoEditPrefabByPath enables an editing for the provided prefab by its path.
func (a *app) DoEditPrefabByPath(path string) {
	log.Println("[app] edit prefab by path:", path)
	a.DoEditPrefab(dmmap.PrefabStorage.Initial(path))
}

// DoSearchPrefab does a search of the provided prefab ID.
func (a *app) DoSearchPrefab(prefabId uint64) {
	log.Println("[app] search prefab id:", prefabId)
	a.layout.Search.Search(prefabId)
}

// DoSearchPrefabByPath does a search of the provided prefab path.
func (a *app) DoSearchPrefabByPath(path string) {
	log.Println("[app] search prefab path:", path)
	a.layout.Search.SearchByPath(path)
}

// DoExit exits the app.
func (a *app) DoExit() {
	log.Println("[app] exit")
	a.tmpShouldClose = true
}

// DoUndo does undo of the latest command.
func (a *app) DoUndo() {
	log.Println("[app] undo")
	a.commandStorage.Undo()
}

// DoRedo does redo of the previous command.
func (a *app) DoRedo() {
	log.Println("[app] redo")
	a.commandStorage.Redo()
}

// DoResetLayout resets application windows to their initial positions.
func (a *app) DoResetLayout() {
	log.Println("[app] reset layout")
	a.resetLayout()
}

// DoOpenChangelog opens "changelog" workspace.
func (a *app) DoOpenChangelog() {
	log.Println("[app] open changelog")
	a.layout.WsArea.OpenChangelog()
}

// DoOpenAbout opens "about" window.
func (a *app) DoOpenAbout() {
	log.Println("[app] open about")
	a.openAboutWindow()
}

// DoOpenLogs opens the logs folder.
func (a *app) DoOpenLogs() {
	log.Println("[app] open logs dir:", a.logDir)
	if err := open.Run(a.logDir); err != nil {
		log.Println("[app] unable to open log dir:", err)
	}
}

// DoOpenSourceCode opens GitHub with a source code for the editor.
func (a *app) DoOpenSourceCode() {
	log.Println("[app] open source code:", env.GitHub)
	if err := open.Run(env.GitHub); err != nil {
		log.Println("[app] unable to open GitHub:", err)
	}
}

// DoOpenSupport opens support page.
func (a *app) DoOpenSupport() {
	log.Println("[app] open source code:", env.Support)
	if err := open.Run(env.Support); err != nil {
		log.Println("[app] unable to open support page:", err)
	}
}

// DoCopy copies currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCopy() {
	log.Println("[app] do copy")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileCopySelected()
	}
}

// DoPaste pastes tiles from the global clipboard on the currently hovered tile.
func (a *app) DoPaste() {
	log.Println("[app] do paste")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TilePasteSelected()
		ws.Map().Editor().CommitChanges("Paste Tile")
	}
}

// DoCut cuts currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCut() {
	log.Println("[app] do cut")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileCutSelected()
		ws.Map().Editor().CommitChanges("Cut Tile")
	}
}

// DoDelete deletes tiles from the currently selected (hovered) tiles.
func (a *app) DoDelete() {
	log.Println("[app] do delete")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileDeleteSelected()
		ws.Map().Editor().CommitChanges("Delete Tile")
	}
}

// DoDeselect deselects currently selected area.
func (a *app) DoDeselect() {
	log.Println("[app] do deselect")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().DoDeselect()
	}
}

// DoSearch searches for a currently selected prefab.
func (a *app) DoSearch() {
	log.Println("[app] do search")
	if prefabId := a.layout.Prefabs.SelectedPrefabId(); prefabId != dmmprefab.IdNone {
		a.DoSearchPrefab(prefabId)
	}
	a.ShowLayout(lnode.NameSearch, true)
}

// DoAreaBorders toggles area borders rendering.
func (a *app) DoAreaBorders() {
	pmap.AreaBordersRendering = !pmap.AreaBordersRendering
	log.Println("[app] do area borders:", pmap.AreaBordersRendering)
}

// DoMultiZRendering toggles multi-z rendering.
func (a *app) DoMultiZRendering() {
	render.MultiZRendering = !render.MultiZRendering
	log.Println("[app] do multiZ rendering:", render.MultiZRendering)
}

// DoMirrorCanvasCamera toggles mode of mirroring canvas camera.
func (a *app) DoMirrorCanvasCamera() {
	pmap.MirrorCanvasCamera = !pmap.MirrorCanvasCamera
	log.Println("[app] do mirror canvas camera:", pmap.MirrorCanvasCamera)
}

// DoSelfUpdate starts the process of a self update.
func (a *app) DoSelfUpdate() {
	log.Println("[app] do self update")
	a.selfUpdate()
}

// DoRestart restarts the application.
func (a *app) DoRestart() {
	log.Println("[app] do restart")
	window.Restart()
}

// DoIgnoreUpdate adds currently available update to to ignore list.
func (a *app) DoIgnoreUpdate() {
	log.Println("[app] do ignore update:", remoteManifest.Version)
	a.config().UpdateIgnore = slice.StrPushUnique(a.config().UpdateIgnore, remoteManifest.Version)
}

// DoCheckForUpdates checks for available update.
func (a *app) DoCheckForUpdates() {
	log.Println("[app] do check for updates")
	a.checkForUpdatesV(true)
}
