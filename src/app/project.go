package app

import (
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/dmapi/dm"
	"time"

	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

func (a *app) openEnvironment(path string) {
	a.layout.WsArea.CloseAllMaps(func(closed bool) {
		if closed {
			a.forceOpenEnvironment(path)
		}
	})
}

func (a *app) forceOpenEnvironment(path string) {
	log.Printf("[app] opening environment [%s]...", path)

	start := time.Now()
	log.Printf("[app] parsing environment: [%s]...", path)
	env, err := dmenv.New(path)
	if err != nil {
		log.Println("[app] unable to open environment:", err)
		util.ShowErrorDialog("Unable to open environment: " + path)
		return
	}
	log.Printf("[app] environment [%s] parsed in [%d] ms", path, time.Since(start).Milliseconds())

	cfg := a.projectConfig()
	cfg.AddProject(path)
	a.ConfigSaveV(cfg)

	// Configure paths filter to access a newly opened environment.
	a.pathsFilter = dm.NewPathsFilter(func(path string) []string {
		return env.Objects[path].DirectChildren
	})

	a.loadedEnvironment = env
	a.layout.Prefabs.Free()
	a.layout.Search.Free()
	a.layout.Environment.Free()
	a.layout.WsArea.Free()
	a.layout.VarEditor.Free()

	a.commandStorage.Free()
	a.clipboard.Free()

	dmicon.Cache.Free()
	dmicon.Cache.SetRootDirPath(env.RootDir)
	dmmap.PrefabStorage.Free()
	dmmap.Init(env)

	a.UpdateTitle()

	runtime.GC()

	log.Println("[app] environment opened:", path)
}

func (a *app) openMap(path string) {
	a.openMapV(path, nil)
}

func (a *app) openMapV(path string, workspace *workspace.Workspace) {
	log.Printf("[app] opening map [%s]...", path)

	start := time.Now()
	log.Printf("[app] parsing map: [%s]...", path)
	data, err := dmmdata.New(path)
	if err != nil {
		log.Printf("[app] unable to open map by path [%s]: %v", path, err)
		return
	}
	elapsed := time.Since(start).Milliseconds()
	log.Printf("[app] map [%s] parsed in [%d] ms", path, elapsed)

	cfg := a.projectConfig()
	cfg.AddMapByProject(a.loadedEnvironment.RootFile, path)
	a.ConfigSaveV(cfg)

	if a.layout.WsArea.OpenMap(dmmap.New(a.loadedEnvironment, data, a.backupMap(path)), workspace) {
		a.layout.Prefabs.Sync()
	}
	a.layout.Search.Free()

	runtime.GC()

	log.Println("[app] map opened:", path)
}

func (a *app) environmentName() string {
	if a.loadedEnvironment != nil {
		return a.loadedEnvironment.Name
	}
	return ""
}

func (a *app) backupMap(path string) string {
	data, err := ioutil.ReadFile(path)
	if err != nil {
		log.Println("[app] unable to read map to backup:", path)
		util.ShowErrorDialog("Unable to read map to backup: " + path)
		os.Exit(1)
	}

	// format: backup/environment.dme/map.dmm/time.dmm
	dst := filepath.FromSlash(a.backupDir + "/" +
		a.environmentName() + "/" +
		filepath.Base(path) + "/" +
		time.Now().Format("2006.01.02-15.04.05") + ".dmm",
	)

	_ = os.MkdirAll(filepath.Dir(dst), os.ModePerm)

	err = ioutil.WriteFile(dst, data, os.ModePerm)
	if err != nil {
		log.Println("[app] unable to write map backup to a file:", dst)
		util.ShowErrorDialog("Unable to write map backup to a file: " + path)
		os.Exit(1)
	}
	log.Println("[app] map backup created:", dst)

	return dst
}
