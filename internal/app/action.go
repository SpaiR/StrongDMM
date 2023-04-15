package app

import (
	"fmt"
	"os"
	"path/filepath"
	"strings"

	"sdmm/internal/app/prefs"
	"sdmm/internal/app/render"
	"sdmm/internal/app/ui/cpwsarea/wsmap"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap/editor"
	"sdmm/internal/dmapi/dmmap/dmminstance"
	"sdmm/internal/env"

	"sdmm/internal/app/command"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/internal/dmapi/dmmclip"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
)

/*
	File contains all the application methods, called by components of the application.
	The idea is that on lower layers we define interfaces with those methods.
	Then we provide the app struct as a high level realization which knows how to handle stuff.
*/

// IsLayoutReset returns true if we reset application layout to its initial state.
func (a *app) IsLayoutReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

// AddMouseChangeCallback adds a new mouse change callback and returns its ID.
// ID could be used to remove callback in the future.
func (a *app) AddMouseChangeCallback(cb func(uint, uint)) (callbackId int) {
	return a.masterWindow.AddMouseChangeCallback(cb)
}

// RemoveMouseChangeCallback removes mouse change callback by associated callback id.
func (a *app) RemoveMouseChangeCallback(callbackId int) {
	a.masterWindow.RemoveMouseChangeCallback(callbackId)
}

// SelectedPrefab returns currently selected dmmdata.Prefab and bool value which shows if there is one.
// Selected prefab is taken from the cpprefabs.Prefabs panel.
func (a *app) SelectedPrefab() (*dmmprefab.Prefab, bool) {
	return dmmap.PrefabStorage.GetById(a.layout.Prefabs.SelectedPrefabId())
}

// HasSelectedPrefab returns true, if the application has a globally selected prefab.
func (a *app) HasSelectedPrefab() bool {
	_, ok := a.SelectedPrefab()
	return ok
}

// SelectedInstance returns currently selected instance.
// Selected instance is taken from the cpvareditor.VarEditor panel.
func (a *app) SelectedInstance() (*dmminstance.Instance, bool) {
	return a.layout.VarEditor.EditedInstance()
}

// HasSelectedInstance returns true, if the application has a globally selected instance.
func (a *app) HasSelectedInstance() bool {
	_, ok := a.SelectedInstance()
	return ok
}

// RecentEnvironments returns a slice with paths to recently opened environments.
func (a *app) RecentEnvironments() []string {
	return a.projectConfig().Projects
}

// RecentMaps returns recent maps from all environments.
func (a *app) RecentMaps() []string {
	return a.projectConfig().Maps
}

// RecentMapsByLoadedEnvironment returns a slice with paths to recently opened maps
// by the currently loaded environment.
func (a *app) RecentMapsByLoadedEnvironment() (recentMaps []string) {
	if a.HasLoadedEnvironment() {
		for _, mapPath := range a.RecentMaps() {
			if strings.Contains(mapPath, a.loadedEnvironment.RootDir) {
				recentMaps = append(recentMaps, mapPath)
			}
		}
	}
	return recentMaps
}

// AvailableMaps returns all maps available for the currently loaded environment.
// Commonly is a blocking operation, so it must be called rarely.
func (a *app) AvailableMaps() (availableMaps []string) {
	if !a.HasLoadedEnvironment() {
		return availableMaps
	}

	err := filepath.Walk(a.LoadedEnvironment().RootDir, func(path string, f os.FileInfo, err error) error {
		if filepath.Ext(path) == ".dmm" {
			availableMaps = append(availableMaps, path)
		}
		return err
	})

	if err != nil {
		log.Print("unable to find available maps:", err)
	}

	return availableMaps
}

// LoadedEnvironment returns currently loaded environment.
func (a *app) LoadedEnvironment() *dmenv.Dme {
	return a.loadedEnvironment
}

// HasLoadedEnvironment returns true if there is any loaded environment.
func (a *app) HasLoadedEnvironment() bool {
	return a.loadedEnvironment != nil
}

// HasActiveMap returns true if there is any active map at the moment.
func (a *app) HasActiveMap() bool {
	_, ok := a.activeWsMap()
	return ok
}

// UpdateTitle updates title in the application system window.
// The title depends on current open environment and workspace.
func (a *app) UpdateTitle() {
	envTitle := a.environmentName()
	wsTitle := a.layout.WsArea.WorkspaceTitle()

	title := ""
	if envTitle != "" {
		if wsTitle != "" {
			title = fmt.Sprintf("%s [%s] - %s", envTitle, wsTitle, env.Title)
		} else {
			title = fmt.Sprintf("%s - %s", envTitle, env.Title)
		}
	} else if wsTitle != "" {
		title = fmt.Sprintf("[%s] - %s", wsTitle, env.Title)
	} else {
		title = env.Title
	}

	a.masterWindow.Handle().SetTitle(title)
	log.Print("title updated:", title)
}

// UpdateScale updates scale of the application.
func (a *app) UpdateScale() {
	a.tmpUpdateScale = true
}

// OnWorkspaceSwitched called when the app workspace is switched.
func (a *app) OnWorkspaceSwitched() {
	a.UpdateTitle()

	// Update search results for the current map.
	if a.HasActiveMap() {
		a.layout.Search.Sync()
	} else {
		a.layout.Search.Free()
	}

	a.SyncVarEditor()
}

// CommandStorage returns command.Storage for the application.
func (a *app) CommandStorage() *command.Storage {
	return a.commandStorage
}

// PathsFilter returns dm.PathsFilter for the application.
func (a *app) PathsFilter() *dm.PathsFilter {
	return a.pathsFilter
}

// Clipboard returns *dmmap.Clipboard for the application.
func (a *app) Clipboard() *dmmclip.Clipboard {
	return a.clipboard
}

// CurrentEditor returns *pmap.Editor for the currently active map.
// Panics when the method called without having the active map.
// We are implying that there won't be any situations like that.
func (a *app) CurrentEditor() *editor.Editor {
	if wsMap, ok := a.activeWsMap(); ok {
		return wsMap.Map().Editor()
	}
	return nil
}

// ShowLayout helps to make sure that a specific layout node is visible (and in a focus).
func (a *app) ShowLayout(name string, focus bool) {
	a.layout.ShowNode(name)
	if focus {
		a.layout.FocusNode(name)
	}
}

// SyncPrefabs updates the prefabs layout list.
func (a *app) SyncPrefabs() {
	a.layout.Prefabs.Sync()
}

// SyncVarEditor syncs the variable editor state. Needed to ensure we edit an object which is exists.
func (a *app) SyncVarEditor() {
	a.layout.VarEditor.Sync()
}

// AreaBordersRendering returns true if an area borders rendering enabled.
func (a *app) AreaBordersRendering() bool {
	return pmap.AreaBordersRendering
}

// MultiZRendering returns true if a multi-z rendering enabled.
func (a *app) MultiZRendering() bool {
	return render.MultiZRendering
}

// MirrorCanvasCamera returns true if maps sync their camera.
func (a *app) MirrorCanvasCamera() bool {
	return pmap.MirrorCanvasCamera
}

// Prefs returns current application preferences.
func (a *app) Prefs() prefs.Prefs {
	return a.preferencesConfig().Prefs
}

// FocusApplicationWindow explicitly moves an OS focus to the current application window.
func (a *app) FocusApplicationWindow() {
	a.masterWindow.Handle().Focus()
}

func (a *app) activeWsMap() (*wsmap.WsMap, bool) {
	if wsMapActive := a.layout.WsArea.ActiveWorkspace(); wsMapActive != nil {
		if activeWs, ok := wsMapActive.Content().(*wsmap.WsMap); ok {
			return activeWs, ok
		}
	}
	return nil, false
}
