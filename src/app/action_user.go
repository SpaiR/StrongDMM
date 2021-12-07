package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
	"sdmm/app/ui/layout/lnode"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmap/dmminstance"
)

/*
	File similar to action.go, but contains methods triggered by user. (ex. when button clicked)
	Such methods have a "Do" prefix, and they are logged excessively.
*/

// DoOpenEnvironment opens environment, which user need to select in file dialog.
func (a *app) DoOpenEnvironment() {
	log.Println("[app] selecting environment to open...")
	if file, err := dialog.
		File().
		Title("Open Environment").
		Filter("*.dme", "dme").
		Load(); err == nil {
		log.Println("[app] environment to open selected:", file)
		a.openEnvironment(file)
	}
}

// DoOpenEnvironmentByPath opens environment by provided path.
func (a *app) DoOpenEnvironmentByPath(path string) {
	log.Println("[app] open environment by path:", path)
	a.openEnvironment(path)
}

// DoClearRecentEnvironments clears recently opened environments.
func (a *app) DoClearRecentEnvironments() {
	log.Println("[app] clear recent environments")
	a.projectConfig().ClearProjects()
	a.ConfigSave()
}

// DoSelectMapFile opens dialog window to select a map file.
func (a *app) DoSelectMapFile() (string, error) {
	log.Println("[app] selecting map file...")
	return dialog.
		File().
		Title("Open Map").
		Filter("*.dmm", "dmm").
		SetStartDir(a.loadedEnvironment.RootDir).
		Load()
}

// DoOpenMap opens map, which user need to select in file dialog.
func (a *app) DoOpenMap() {
	log.Println("[app] selecting map to open...")
	if file, err := a.DoSelectMapFile(); err == nil {
		log.Println("[app] map to open selected:", file)
		a.openMap(file)
	}
}

// DoOpenMapByPath opens map by provided path.
func (a *app) DoOpenMapByPath(path string) {
	log.Println("[app] open map by path:", path)
	a.openMap(path)
}

// DoOpenMapByPathV same as DoOpenMapByPath by map will be opened inside the concrete workspace with the provided index.
func (a *app) DoOpenMapByPathV(path string, workspaceIdx int) {
	log.Printf("[app] open map with workspace index [%d] by path: [%s]", workspaceIdx, path)
	a.openMapV(path, workspaceIdx)
}

// DoClearRecentMaps clears recently opened maps.
func (a *app) DoClearRecentMaps() {
	log.Println("[app] clear recent maps")
	a.projectConfig().ClearMapsByProject(a.loadedEnvironment.RootFile)
	a.ConfigSave()
}

// DoSave saves current active map.
func (a *app) DoSave() {
	log.Println("[app] do save")
	if ws, ok := a.activeWsMap(); ok {
		ws.Save()
	}
}

// DoOpenPreferences opens preferences tab.
func (a *app) DoOpenPreferences() {
	log.Println("[app] open preferences")
	a.layout.OpenPreferences(a.makePreferences())
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

// DoSearchPrefab does a search of the provided prefab ID with selecting of the search window.
func (a *app) DoSearchPrefab(prefabId uint64) {
	log.Println("[app] search prefab id:", prefabId)
	a.layout.Search.Search(prefabId)
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

// DoOpenLogs opens the logs folder.
func (a *app) DoOpenLogs() {
	log.Println("[app] open logs dir:", a.logDir)
	if err := open.Run(a.logDir); err != nil {
		log.Println("[app] unable to open log dir:", err)
	}
}

// DoCopy copies currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCopy() {
	log.Println("[app] do copy")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().CopyHoveredTile()
	}
}

// DoPaste pastes tiles from the global clipboard on the currently hovered tile.
func (a *app) DoPaste() {
	log.Println("[app] do paste")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().PasteHoveredTile()
	}
}

// DoCut cuts currently selected (hovered) tiles to the global clipboard.
func (a *app) DoCut() {
	log.Println("[app] do cut")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().CutHoveredTile()
	}
}

// DoDelete deletes tiles from the currently selected (hovered) tiles.
func (a *app) DoDelete() {
	log.Println("[app] do delete")
	if ws, ok := a.activeWsMap(); ok {
		ws.Map().Editor().DeleteHoveredTile(true)
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
