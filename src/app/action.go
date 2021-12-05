package app

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata/dmmprefab"
	"sdmm/dmapi/dmmclip"
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

// PointSize returns application point size.
// Point size is a value which helps to scale the application GUI.
// For 100% scale points size will be 1. For 200% it will be 2.
// So we multiply all the application sizes (like the font size) to the point size and get sort of DPI support.
func (a *app) PointSize() float32 {
	return a.masterWindow.PointSize()
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

// RecentEnvironments returns a slice with paths to recently opened environments.
func (a *app) RecentEnvironments() []string {
	return a.projectConfig().Projects
}

// RecentMapsByEnvironment returns a map with key as an environment path and value as a slice of maps opened
// for the environment path.
func (a *app) RecentMapsByEnvironment() map[string][]string {
	return a.projectConfig().MapsByProject
}

// RecentMapsByLoadedEnvironment returns a slice with paths to recently opened maps
// by the currently loaded environment.
func (a *app) RecentMapsByLoadedEnvironment() []string {
	if a.HasLoadedEnvironment() {
		return a.RecentMapsByEnvironment()[a.loadedEnvironment.RootFile]
	}
	return nil
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
	if activeWs := a.layout.WsArea.ActiveWorkspace(); activeWs != nil {
		_, ok := activeWs.(*wsmap.WsMap)
		return ok
	}
	return false
}

// UpdateTitle updates title in the application system window.
// The title depends on current open environment and workspace.
func (a *app) UpdateTitle() {
	envTitle := a.environmentName()
	wsTitle := a.layout.WsArea.WorkspaceTitle()

	title := ""
	if envTitle != "" {
		if wsTitle != "" {
			title = fmt.Sprintf("%s [%s] - %s", envTitle, wsTitle, Title)
		} else {
			title = fmt.Sprintf("%s - %s", envTitle, Title)
		}
	} else {
		title = Title
	}

	a.masterWindow.Handle().SetTitle(title)
	log.Println("[app] title updated:", title)
}

// OnWorkspaceSwitched called when the app workspace is switched.
func (a *app) OnWorkspaceSwitched() {
	a.UpdateTitle()
	a.layout.Search.Free()
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
func (a *app) CurrentEditor() *pmap.Editor {
	if wsMap, ok := a.activeWsMap(); ok {
		return wsMap.Map().Editor()
	}
	log.Panic("[app] requesting for the editor without having one!")
	return nil
}

// ToggleShortcuts toggles shortcuts processing for the whole application.
// Helps in cases where there are shortcuts on the Dear ImGui side (basically, input fields) and we need them to work.
func (a *app) ToggleShortcuts(enabled bool) {
	log.Println("[app] shortcuts toggled:", enabled)
	a.shortcutsEnabled = enabled
}

func (a *app) activeWsMap() (*wsmap.WsMap, bool) {
	if activeWs := a.layout.WsArea.ActiveWorkspace(); activeWs != nil {
		if activeWs, ok := activeWs.(*wsmap.WsMap); ok {
			return activeWs, ok
		}
	}
	return nil, false
}
