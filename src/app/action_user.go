package app

import (
	"log"

	"github.com/skratchdot/open-golang/open"
	"github.com/sqweek/dialog"
	"sdmm/app/ui/cpwsarea/workspace/wsmap"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
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
	a.configData.ClearRecentEnvironments()
	a.configData.Save()
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
	a.configData.ClearRecentMaps(a.loadedEnvironment.RootFile)
	a.configData.Save()
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

// DoSelectInstance globally selects provided instance in the app.
func (a *app) DoSelectInstance(instance *dmmdata.Instance) {
	log.Printf("[app] select instance: path=[%s], id=[%d]", instance.Path(), instance.Id())
	a.layout.Environment.SelectPath(instance.Path())
	a.layout.Instances.Select(instance)
}

// DoSelectInstanceByPath globally selects an instance with provided type path.
func (a *app) DoSelectInstanceByPath(path string) {
	log.Println("[app] select instance by path:", path)
	a.DoSelectInstance(dmmap.InstanceCache.Get(path, a.InitialInstanceVariables(path)))
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

// DoResetWindows resets application windows to their initial positions.
func (a *app) DoResetWindows() {
	log.Println("[app] reset windows")
	a.resetWindows()
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
		ws.PaneMap.CopyTiles()
	}
}

// DoPaste pastes tiles from the global clipboard on the currently hovered tile.
func (a *app) DoPaste() {
	log.Println("[app] do paste")
	if ws, ok := a.activeWsMap(); ok {
		ws.PaneMap.PasteTiles()
	}
}

func (a *app) DoCut() {
	log.Println("[app] do cut")
	if ws, ok := a.activeWsMap(); ok {
		ws.PaneMap.CutTiles()
	}
}

// DoDelete deletes tiles from the currently selected (hovered) tiles.
func (a *app) DoDelete() {
	log.Println("[app] do delete")
	if ws, ok := a.activeWsMap(); ok {
		ws.PaneMap.DeleteTiles()
	}
}

func (a *app) activeWsMap() (*wsmap.WsMap, bool) {
	if activeWs := a.layout.WsArea.ActiveWorkspace(); activeWs != nil {
		if activeWs, ok := activeWs.(*wsmap.WsMap); ok {
			return activeWs, ok
		}
	}
	return nil, false
}
