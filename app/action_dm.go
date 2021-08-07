package app

import (
	"log"

	"github.com/SpaiR/strongdmm/pkg/dm/dmmap/dmminstance"
	"github.com/sqweek/dialog"

	"github.com/SpaiR/strongdmm/pkg/dm/dmenv"
)

func (a *app) LoadedEnvironment() *dmenv.Dme {
	return a.loadedEnvironment
}

func (a *app) HasLoadedEnvironment() bool {
	return a.loadedEnvironment != nil
}

func (a *app) DoOpenEnvironment() {
	log.Println("[app] opening environment")
	if file, err := dialog.File().Title("Open Environment").Filter("*.dme", "dme").Load(); err == nil {
		a.openEnvironment(file)
	}
}

func (a *app) DoOpenEnvironmentByPath(path string) {
	log.Println("[app] opening environment by path:", path)
	a.openEnvironment(path)
}

func (a *app) DoClearRecentEnvironments() {
	log.Println("[app] clearing recent environments")
	a.internalData.ClearRecentEnvironments()
	a.internalData.Save()
}

func (a *app) DoOpenMap() {
	log.Println("[app] opening map")
	if file, err := dialog.File().Title("Open Map").Filter("*.dmm", "dmm").SetStartDir(a.loadedEnvironment.RootDir).Load(); err == nil {
		a.openMap(file)
	}
}

func (a *app) DoOpenMapByPath(path string) {
	log.Println("[app] opening map by path:", path)
	a.openMap(path)
}

func (a *app) DoClearRecentMaps() {
	log.Println("[app] clearing recent maps")
	a.internalData.ClearRecentMaps(a.loadedEnvironment.RootFile)
	a.internalData.Save()
}

func (a *app) RecentEnvironments() []string {
	return a.internalData.RecentEnvironments
}

func (a *app) RecentMapsByEnvironment() map[string][]string {
	return a.internalData.RecentMapsByEnvironment
}

func (a *app) RecentMapsByLoadedEnvironment() []string {
	if a.HasLoadedEnvironment() {
		return a.RecentMapsByEnvironment()[a.loadedEnvironment.RootFile]
	}
	return nil
}

func (a *app) DoSelectInstance(instance *dmminstance.Instance) {
	a.layout.Environment.SelectPath(instance.Path)
	a.layout.Instances.Select(instance)
}

// SelectedInstance returns currently selected *dmminstance.Instance or nil.
// Selected instance is taken from the component.Instances panel.
func (a *app) SelectedInstance() *dmminstance.Instance {
	return dmminstance.Cache.GetById(a.layout.Instances.SelectedInstanceId())
}
