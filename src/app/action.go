package app

import (
	"github.com/SpaiR/imgui-go"
	"strongdmm/app/command"
	"strongdmm/dm/dmenv"
	"strongdmm/dm/dmmap/dmminstance"
)

/*
	File contains all the app package methods, called by the app itself.
	The idea is that on lower layers we define interfaces with those methods.
	And then we provide app struct as a high level realization.
*/

func (a *app) AppWindowCond() imgui.Condition {
	return a.tmpWindowCond
}

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

// AppMasterWindowSize returns sizes of the application system window.
func (a *app) AppMasterWindowSize() (width, height int) {
	return a.masterWindow.Handle.GetSize()
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

// AppSelectedInstance returns currently selected *dmminstance.Instance or nil.
// Selected instance is taken from the component.Instances panel.
func (a *app) AppSelectedInstance() *dmminstance.Instance {
	return dmminstance.Cache.GetById(a.layout.Instances.SelectedInstanceId())
}

// AppHasSelectedInstance returns true, if the application has a globally selected instance.
func (a *app) AppHasSelectedInstance() bool {
	return a.AppSelectedInstance() != nil
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
