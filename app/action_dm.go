package app

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	"github.com/sqweek/dialog"

	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
)

func (a *app) AppLoadedEnvironment() *dmenv.Dme {
	return a.loadedEnvironment
}

func (a *app) AppHasLoadedEnvironment() bool {
	return a.loadedEnvironment != nil
}

func (a *app) AppDoOpenEnvironment() {
	log.Println("[app] opening environment")
	if file, err := dialog.
		File().
		Title("Open Environment").
		Filter("*.dme", "dme").
		Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) AppDoOpenEnvironmentByPath(path string) {
	log.Println("[app] opening environment by path:", path)
	a.openEnvironment(path)
}

func (a *app) AppDoClearRecentEnvironments() {
	log.Println("[app] clearing recent environments")
	a.internalData.ClearRecentEnvironments()
	a.internalData.Save()
}

func (a *app) AppDoOpenMap() {
	log.Println("[app] opening map")
	if file, err := dialog.
		File().
		Title("Open Map").
		Filter("*.dmm", "dmm").
		SetStartDir(a.loadedEnvironment.RootDir).
		Load(); err == nil {
		a.openMap(file)
	}
}

func (a *app) AppDoOpenMapByPath(path string) {
	log.Println("[app] opening map by path:", path)
	a.openMap(path)
}

func (a *app) AppDoClearRecentMaps() {
	log.Println("[app] clearing recent maps")
	a.internalData.ClearRecentMaps(a.loadedEnvironment.RootFile)
	a.internalData.Save()
}

func (a *app) AppRecentEnvironments() []string {
	return a.internalData.RecentEnvironments
}

func (a *app) AppRecentMapsByEnvironment() map[string][]string {
	return a.internalData.RecentMapsByEnvironment
}

func (a *app) AppRecentMapsByLoadedEnvironment() []string {
	if a.AppHasLoadedEnvironment() {
		return a.AppRecentMapsByEnvironment()[a.loadedEnvironment.RootFile]
	}
	return nil
}

func (a *app) AppDoSelectInstance(instance *dmminstance.Instance) {
	a.layout.Environment.SelectPath(instance.Path)
	a.layout.Instances.Select(instance)
}

// AppSelectedInstance returns currently selected *dmminstance.Instance or nil.
// Selected instance is taken from the component.Instances panel.
func (a *app) AppSelectedInstance() *dmminstance.Instance {
	return dmminstance.Cache.GetById(a.layout.Instances.SelectedInstanceId())
}

func (a *app) AppHasSelectedInstance() bool {
	return a.AppSelectedInstance() != nil
}
