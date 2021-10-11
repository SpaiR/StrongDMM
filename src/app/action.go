package app

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace/wsmap"
	"sdmm/dm"
	"sdmm/dm/dmenv"
	"sdmm/dm/dmmap"
	"sdmm/dm/dmmap/dmmdata"
	"sdmm/dm/dmvars"
)

/*
	File contains all the application methods, called by components of the application.
	The idea is that on lower layers we define interfaces with those methods.
	Then we provide the app struct as a high level realization which knows how to handle stuff.
*/

// IsWindowReset returns true if we reset application windows to their initial positions.
func (a *app) IsWindowReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

// PointSize returns application point size.
// Point size is a value which helps to scale the application GUI.
// For 100% scale points size will be 1. For 200% it will be 2.
// So we multiply all the application sizes (like the font size) to the point size and get sort of DPI support.
func (a *app) PointSize() float32 {
	return a.masterWindow.PointSize()
}

// RunLater ques received function to execute it later.
// Basically, later jobs will be executed at the beginning of the next frame.
func (a *app) RunLater(job func()) {
	a.masterWindow.RunLater(job)
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

// SelectedInstance returns currently selected dmmdata.Instance and bool value which shows if there is one.
// Selected instance is taken from the component.Instances panel.
func (a *app) SelectedInstance() (*dmmdata.Instance, bool) {
	return dmmap.InstanceCache.GetById(a.layout.Instances.SelectedInstanceId())
}

// HasSelectedInstance returns true, if the application has a globally selected instance.
func (a *app) HasSelectedInstance() bool {
	_, ok := a.SelectedInstance()
	return ok
}

// RecentEnvironments returns a slice with paths to recently opened environments.
func (a *app) RecentEnvironments() []string {
	return a.configData.RecentEnvironments
}

// RecentMapsByEnvironment returns a map with key as an environment path and value as a slice of maps opened
// for the environment path.
func (a *app) RecentMapsByEnvironment() map[string][]string {
	return a.configData.RecentMapsByEnvironment
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

// EnvironmentObjectVariables returns initial variables for an environment object with provided path.
func (a *app) EnvironmentObjectVariables(path string) *dmvars.Variables {
	return a.loadedEnvironment.Objects[path].Vars
}

// InitialInstanceVariables returns initial variables for an instance of the map with provided path.
// Initial variables don't have any internal data.
// They have a parent, as variables of the appropriate environment object.
func (a *app) InitialInstanceVariables(path string) *dmvars.Variables {
	return dmvars.FromParent(a.EnvironmentObjectVariables(path))
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

	a.masterWindow.Handle.SetTitle(title)
	log.Println("[app] title updated:", title)
}

// CommandStorage returns command.Storage for the application.
func (a *app) CommandStorage() *command.Storage {
	return a.commandStorage
}

// PathsFilter returns dm.PathsFilter for the application.
func (a *app) PathsFilter() *dm.PathsFilter {
	return a.pathsFilter
}
