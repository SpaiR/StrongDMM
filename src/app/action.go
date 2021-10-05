package app

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/component/workspace"
	"sdmm/dm/dmenv"
	"sdmm/dm/dmmap/dmminstance"
	"sdmm/dm/dmvars"
)

/*
	File contains all the application methods, called by components of the application.
	The idea is that on lower layers we define interfaces with those methods.
	Then we provide the app struct as a high level realization which knows how to handle stuff.
*/

// AppIsWindowReset returns true if we reset application windows to their initial positions.
func (a *app) AppIsWindowReset() bool {
	return a.tmpWindowCond == imgui.ConditionAlways
}

// AppPointSize returns application point size.
// Point size is a value which helps to scale the application GUI.
// For 100% scale points size will be 1. For 200% it will be 2.
// So we multiply all the application sizes (like the font size) to the point size and get sort of DPI support.
func (a *app) AppPointSize() float32 {
	return a.masterWindow.PointSize
}

// AppPointSizePtr returns same value as the AppPointSize, but in a form of the pointer.
func (a *app) AppPointSizePtr() *float32 {
	return &a.masterWindow.PointSize
}

// AppRunLater ques received function to execute it later.
// Basically, later jobs will be executed at the beginning of the next frame.
func (a *app) AppRunLater(job func()) {
	a.masterWindow.AppRunLater(job)
}

// AppAddMouseChangeCallback adds a new mouse change callback and returns its ID.
// ID could be used to remove callback in the future.
func (a *app) AppAddMouseChangeCallback(cb func(uint, uint)) (callbackId int) {
	return a.masterWindow.AddMouseChangeCallback(cb)
}

// AppRemoveMouseChangeCallback removes mouse change callback by associated callback id.
func (a *app) AppRemoveMouseChangeCallback(callbackId int) {
	a.masterWindow.RemoveMouseChangeCallback(callbackId)
}

// AppSwitchCommandStack switches application active command stack id.
func (a *app) AppSwitchCommandStack(id string) {
	a.commandStorage.SetStack(id)
}

// AppPushCommand pushes received command to the active command stack.
func (a *app) AppPushCommand(command command.Command) {
	a.commandStorage.Push(command)
}

// AppHasUndo returns true, if the current active command stack has any command to undo.
func (a *app) AppHasUndo() bool {
	return a.commandStorage.HasUndo()
}

// AppHasRedo returns true, if the current active command stack has any command to redo.
func (a *app) AppHasRedo() bool {
	return a.commandStorage.HasRedo()
}

// AppIsCommandStackModified returns true, if the command stack with provided id is modified.
func (a *app) AppIsCommandStackModified(id string) bool {
	return a.commandStorage.IsModified(id)
}

// AppForceBalanceCommandStack will do a force balance of the command stack with provided id.
func (a *app) AppForceBalanceCommandStack(id string) {
	a.commandStorage.ForceBalance(id)
}

// AppDisposeCommandStack will dispose the command stack with provided id.
func (a *app) AppDisposeCommandStack(id string) {
	a.commandStorage.DisposeStack(id)
}

// AppSelectedInstance returns currently selected dmminstance.Instance and bool value which shows if there is one.
// Selected instance is taken from the component.Instances panel.
func (a *app) AppSelectedInstance() (dmminstance.Instance, bool) {
	return dmminstance.Cache.GetById(a.layout.Instances.SelectedInstanceId())
}

// AppHasSelectedInstance returns true, if the application has a globally selected instance.
func (a *app) AppHasSelectedInstance() bool {
	_, ok := a.AppSelectedInstance()
	return ok
}

// AppRecentEnvironments returns a slice with paths to recently opened environments.
func (a *app) AppRecentEnvironments() []string {
	return a.internalData.RecentEnvironments
}

// AppRecentMapsByEnvironment returns a map with key as an environment path and value as a slice of maps opened
// for the environment path.
func (a *app) AppRecentMapsByEnvironment() map[string][]string {
	return a.internalData.RecentMapsByEnvironment
}

// AppRecentMapsByLoadedEnvironment returns a slice with paths to recently opened maps
// by the currently loaded environment.
func (a *app) AppRecentMapsByLoadedEnvironment() []string {
	if a.AppHasLoadedEnvironment() {
		return a.AppRecentMapsByEnvironment()[a.loadedEnvironment.RootFile]
	}
	return nil
}

// AppLoadedEnvironment returns currently loaded environment.
func (a *app) AppLoadedEnvironment() *dmenv.Dme {
	return a.loadedEnvironment
}

// AppHasLoadedEnvironment returns true if there is any loaded environment.
func (a *app) AppHasLoadedEnvironment() bool {
	return a.loadedEnvironment != nil
}

// AppHasActiveMap returns true if there is any active map at the moment.
func (a *app) AppHasActiveMap() bool {
	if activeWs := a.layout.WorkspaceArea.ActiveWorkspace(); activeWs != nil {
		_, ok := activeWs.(*workspace.Map)
		return ok
	}
	return false
}

// AppEnvironmentObjectVariables returns initial variables for an environment object with provided path.
func (a *app) AppEnvironmentObjectVariables(path string) *dmvars.Variables {
	return a.loadedEnvironment.Objects[path].Vars
}

// AppInitialInstanceVariables returns initial variables for an instance of the map with provided path.
// Initial variables don't have any internal data.
// They have a parent, as variables of the appropriate environment object.
func (a *app) AppInitialInstanceVariables(path string) *dmvars.Variables {
	vars := &dmvars.Variables{}
	vars.SetParent(a.AppEnvironmentObjectVariables(path))
	return vars
}

// AppUpdateTitle updates title in the application system window.
// The title depends on current open environment and workspace.
func (a *app) AppUpdateTitle() {
	envTitle := a.environmentName()
	wsTitle := a.layout.WorkspaceArea.WorkspaceTitle()

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
