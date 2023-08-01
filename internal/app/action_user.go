package app

import (
	"strconv"

	"sdmm/internal/app/prefs"
	"sdmm/internal/app/render"
	"sdmm/internal/app/ui/cpwsarea/workspace"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/internal/app/ui/layout/lnode"
	"sdmm/internal/app/window"
	"sdmm/internal/util"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/env"
	"sdmm/internal/util/slice"

	"github.com/rs/zerolog/log"
	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
	"github.com/SpaiR/imgui-go"
	dial "sdmm/internal/app/ui/dialog"
)

/*
	File similar to action.go, but contains methods triggered by user. (ex. when button clicked)
	Such methods have a "Do" prefix, and they are logged excessively.
*/

func (a *app) DoNewWorkspace() {
	log.Print("new workspace...")
	a.layout.WsArea.AddEmptyWorkspace()
}

// DoOpen opens environment, which user need to select in file dialog.
func (a *app) DoOpen() {
	a.DoOpenV(nil)
}

// DoOpenV opens environment, which user need to select in file dialog.
// Verbose version which handle opening in a specific workspace. Helpful to open maps.
func (a *app) DoOpenV(ws *workspace.Workspace) {
	log.Print("selecting resource to load...")

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
		log.Print("resource to load selected:", file)

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
// Verbose version which handle opening in a specific workspace. Helpful to open maps.
func (a *app) DoLoadResourceV(path string, ws *workspace.Workspace) {
	log.Print("load resource by path:", path)
	a.loadResourceV(path, ws)
}

// DoClearRecentEnvironments clears recently opened environments.
func (a *app) DoClearRecentEnvironments() {
	log.Print("clear recent environments")
	a.projectConfig().ClearProjects()
}

// DoCloseEnvironment closes currently opened environment.
func (a *app) DoCloseEnvironment() {
	log.Print("closing environment")
	a.closeEnvironment(func(closed bool) {
		if closed {
			a.freeEnvironmentResources()
			a.layout.WsArea.AddEmptyWorkspaceIfNone()
		}
	})
}

// DoNewMap opens dialog window to create a new map file.
func (a *app) DoNewMap() {
	log.Print("opening create map...")
	a.layout.WsArea.OpenCreateMap()
}

// DoClearRecentMaps clears recently opened maps.
func (a *app) DoClearRecentMaps() {
	log.Print("clear recent maps")
	a.projectConfig().ClearMaps()
}

// DoRemoveRecentMaps removes specific recent maps.
func (a *app) DoRemoveRecentMaps(recentMaps []string) {
	log.Print("do remove recent maps:", recentMaps)
	recentMaps = append(make([]string, 0, len(recentMaps)), recentMaps...)
	for _, recentMap := range recentMaps {
		a.projectConfig().RemoveMap(recentMap)
	}
}

// DoRemoveRecentEnvironment removes specific recent environment.
func (a *app) DoRemoveRecentEnvironment(envPath string) {
	log.Print("remove recent environment:", envPath)
	a.projectConfig().RemoveEnvironment(envPath)
}

// DoRemoveRecentMap removes specific recent map.
func (a *app) DoRemoveRecentMap(mapPath string) {
	log.Print("remove recent map:", mapPath)
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
	log.Print("do save")
	if ws, ok := a.activeWsMap(); ok {
		ws.Save()
	}
}

// DoSaveAll saves all active maps.
func (a *app) DoSaveAll() {
	log.Print("do save all")
	for _, ws := range a.layout.WsArea.MapWorkspaces() {
		ws.Save()
	}
}

// DoOpenPreferences opens preferences tab.
func (a *app) DoOpenPreferences() {
	log.Print("open preferences")
	a.layout.WsArea.OpenPreferences(prefs.Make(a, &a.preferencesConfig().Prefs))
}

// DoSelectPrefab globally selects provided prefab in the app.
func (a *app) DoSelectPrefab(prefab *dmmprefab.Prefab) {
	log.Printf("select prefab: path=[%s], id=[%d]", prefab.Path(), prefab.Id())
	a.layout.Environment.SelectPath(prefab.Path())
	a.layout.Prefabs.Select(prefab)
}

// DoSelectPrefabByPath globally selects a prefab with provided type path.
func (a *app) DoSelectPrefabByPath(path string) {
	log.Print("select prefab by path:", path)
	a.DoSelectPrefab(dmmap.PrefabStorage.Initial(path))
}

// DoEditInstance enables an editing for the provided instance.
func (a *app) DoEditInstance(instance *dmminstance.Instance) {
	log.Print("edit instance:", instance.Id())
	a.layout.VarEditor.EditInstance(instance)
}

// DoEditPrefab enables an editing for the provided prefab.
func (a *app) DoEditPrefab(prefab *dmmprefab.Prefab) {
	log.Print("edit prefab:", prefab.Id())
	a.layout.VarEditor.EditPrefab(prefab)
}

// DoEditPrefabByPath enables an editing for the provided prefab by its path.
func (a *app) DoEditPrefabByPath(path string) {
	log.Print("edit prefab by path:", path)
	a.DoEditPrefab(dmmap.PrefabStorage.Initial(path))
}

// DoSearchPrefab does a search of the provided prefab ID.
func (a *app) DoSearchPrefab(prefabId uint64) {
	log.Print("search prefab id:", prefabId)
	a.layout.Search.Search(prefabId)
}

// DoSearchPrefabByPath does a search of the provided prefab path.
func (a *app) DoSearchPrefabByPath(path string) {
	log.Print("search prefab path:", path)
	a.layout.Search.SearchByPath(path)
}

// DoExit exits the app.
func (a *app) DoExit() {
	log.Print("exit")
	a.tmpShouldClose = true
}

// DoUndo does undo of the latest command.
func (a *app) DoUndo() {
	log.Print("undo")
	a.commandStorage.Undo()
}

// DoRedo does redo of the previous command.
func (a *app) DoRedo() {
	log.Print("redo")
	a.commandStorage.Redo()
}

// DoResetLayout resets application windows to their initial positions.
func (a *app) DoResetLayout() {
	log.Print("reset layout")
	a.resetLayout()
}

// DoOpenChangelog opens "changelog" workspace.
func (a *app) DoOpenChangelog() {
	log.Print("open changelog")
	a.layout.WsArea.OpenChangelog()
}

// DoOpenAbout opens "about" window.
func (a *app) DoOpenAbout() {
	log.Print("open about")
	a.openAboutWindow()
}

// DoOpenLogs opens the logs folder.
func (a *app) DoOpenLogs() {
	log.Print("open logs dir:", a.logDir)
	if err := open.Run(a.logDir); err != nil {
		log.Print("unable to open log dir:", err)
	}
}

// DoOpenSourceCode opens GitHub with a source code for the editor.
func (a *app) DoOpenSourceCode() {
	log.Print("open source code:", env.GitHub)
	if err := open.Run(env.GitHub); err != nil {
		log.Print("unable to open GitHub:", err)
	}
}

// DoOpenSupport opens support page.
func (a *app) DoOpenSupport() {
	log.Print("open source code:", env.Support)
	if err := open.Run(env.Support); err != nil {
		log.Print("unable to open support page:", err)
	}
}

// DoCopy copies currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCopy() {
	log.Print("do copy")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileCopySelected()
	}
}

// DoPaste pastes tiles from the global clipboard on the currently hovered tile.
func (a *app) DoPaste() {
	log.Print("do paste")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TilePasteSelected()
		ws.Map().Editor().CommitChanges("Paste Tile")
	}
}

// DoCut cuts currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCut() {
	log.Print("do cut")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileCutSelected()
		ws.Map().Editor().CommitChanges("Cut Tile")
	}
}

// DoDelete deletes tiles from the currently selected (hovered) tiles.
func (a *app) DoDelete() {
	log.Print("do delete")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().TileDeleteSelected()
		ws.Map().Editor().CommitChanges("Delete Tile")
	}
}

// DoDeselect deselects currently selected area.
func (a *app) DoDeselect() {
	log.Print("do deselect")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().DoDeselect()
	}
}

// DoSearch searches for a currently selected prefab.
func (a *app) DoSearch() {
	log.Print("do search")
	if prefabId := a.layout.Prefabs.SelectedPrefabId(); prefabId != dmmprefab.IdNone {
		a.DoSearchPrefab(prefabId)
	}
	a.ShowLayout(lnode.NameSearch, true)
}

// DoAreaBorders toggles area borders rendering.
func (a *app) DoAreaBorders() {
	pmap.AreaBordersRendering = !pmap.AreaBordersRendering
	log.Print("do area borders:", pmap.AreaBordersRendering)
}

// DoMultiZRendering toggles multi-z rendering.
func (a *app) DoMultiZRendering() {
	render.MultiZRendering = !render.MultiZRendering
	log.Print("do multiZ rendering:", render.MultiZRendering)
}

// DoMirrorCanvasCamera toggles mode of mirroring canvas camera.
func (a *app) DoMirrorCanvasCamera() {
	pmap.MirrorCanvasCamera = !pmap.MirrorCanvasCamera
	log.Print("do mirror canvas camera:", pmap.MirrorCanvasCamera)
}

// DoSelfUpdate starts the process of a self update.
func (a *app) DoSelfUpdate() {
	log.Print("do self update")
	a.selfUpdate()
}

// DoRestart restarts the application.
func (a *app) DoRestart() {
	log.Print("do restart")
	window.Restart()
}

// DoIgnoreUpdate adds currently available update to to ignore list.
func (a *app) DoIgnoreUpdate() {
	log.Print("do ignore update:", remoteManifest.Version)
	a.config().UpdateIgnore = slice.StrPushUnique(a.config().UpdateIgnore, remoteManifest.Version)
}

// DoCheckForUpdates checks for available update.
func (a *app) DoCheckForUpdates() {
	log.Print("do check for updates")
	a.checkForUpdatesV(true)
}

// DoSearch searches for a currently selected prefab.
func (a *app) DoOpenJumpWindow() {
	log.Print("do jump")
	var x,y,z string
	z = strconv.Itoa(a.CurrentEditor().ActiveLevel())
	dial.Open(dial.TypeCustom{
		Title:       "Jump/Go To Coordinate",
		CloseButton: true,
		Layout: w.Layout{
			w.AlignTextToFramePadding(),
			w.InputTextWithHint("##x", "X", &x),
			w.InputTextWithHint("##y", "Y", &y),
			w.InputTextWithHint("##z","Z", &z),
			w.Button("Jump!", func() {
				intX, errX := strconv.Atoi(x)
				intY, errY := strconv.Atoi(y)
				intZ, errZ := strconv.Atoi(z)
				if errX == nil && errY == nil && errZ == nil {
					e := a.CurrentEditor()
					e.FocusCameraOnPosition(util.Point{X: intX,Y: intY,Z: intZ})
					imgui.CloseCurrentPopup()
				}
			}),
		},
	})
}
