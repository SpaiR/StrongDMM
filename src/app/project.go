package app

import (
	"fmt"
	"io/ioutil"
	"log"
	"os"
	"path/filepath"
	"runtime"
	"time"

	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/dialog"
	"sdmm/dmapi/dm"
	"sdmm/util/slice"

	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmicon"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmap/dmmdata"
	"sdmm/util"
)

// Universal method to open any editor resource.
// If it gets a map file, then the code will try to find an environment to open it.
func (a *app) loadResource(path string, ws *workspace.Workspace) {
	if filepath.Ext(path) == ".dme" {
		a.loadEnvironment(path)
		return
	}

	if filepath.Ext(path) != ".dmm" {
		log.Println("[app] invalid resource to load:", path)
		return
	}

	environmentPath, err := findEnvironmentFileFromBase(path)

	if a.HasLoadedEnvironment() && a.LoadedEnvironment().RootFile == environmentPath {
		a.loadMapV(path, ws)
		return
	}

	if err == nil {
		a.loadEnvironmentV(environmentPath, func() {
			a.loadMapV(path, ws)
		})
	} else {
		log.Println("[app] unable to find environment from file:", path)
		dialog.Open(dialog.TypeInformation{
			Title: "No dme found!",
			Information: "Can't find an environment file.\n" +
				"Please, ensure it can be accessed or open manually.\n" +
				path,
		})
	}
}

// Goes through all parents starting from the current file location and look for a ".dme" file
func findEnvironmentFileFromBase(path string) (string, error) {
	for {
		dir := filepath.Dir(path)

		if dir == path {
			return "", fmt.Errorf("unable to find environment")
		}

		files, err := ioutil.ReadDir(dir)
		if err != nil {
			log.Println("[app] unable to read dir while looking for environment:", err)
			return "", fmt.Errorf("unable to read dir: "+dir, err)
		}

		for _, file := range files {
			if filepath.Ext(file.Name()) == ".dme" {
				return filepath.Join(dir, file.Name()), nil
			}
		}

		path = filepath.Dir(path)
	}
}

func (a *app) loadEnvironment(path string) {
	a.loadEnvironmentV(path, nil)
}

func (a *app) loadEnvironmentV(path string, callback func()) {
	// NewMap workspaces depend on the opened environment, so we close them too.
	a.layout.WsArea.CloseAllNewMaps()
	a.layout.WsArea.CloseAllMaps(func(closed bool) {
		if closed {
			a.forceLoadEnvironment(path, callback)
		}
	})
}

func (a *app) forceLoadEnvironment(path string, callback func()) {
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

	a.layout.WsArea.AddEmptyWorkspaceIfNone()
	a.UpdateTitle()

	runtime.GC()

	log.Println("[app] environment opened:", path)

	if callback != nil {
		callback()
	}
}

func (a *app) loadMap(path string) {
	a.loadMapV(path, nil)
}

func (a *app) loadMapV(path string, workspace *workspace.Workspace) {
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

	// Add map to the recent only if it is a part of the currently opened environment.
	if slice.StrContains(a.AvailableMaps(), path) {
		log.Println("[app] adding map path to the recent:", path)
		cfg := a.projectConfig()
		cfg.AddMapByProject(a.loadedEnvironment.RootFile, path)
	} else {
		log.Println("[app] ignoring map path add to the recent, since it's an outside resource")
	}

	dmm, unknownPrefabs := dmmap.New(a.loadedEnvironment, data, a.backupMap(path))
	if a.layout.WsArea.OpenMap(dmm, workspace) {
		a.layout.Prefabs.Sync()

		// TODO: processing for unknown prefabs
		if len(unknownPrefabs) != 0 {
			var prefabsNames string
			for path := range unknownPrefabs {
				prefabsNames += " - " + path + "\n"
			}

			dialog.Open(dialog.TypeInformation{
				Title: "Unknown Types [WIP]",
				Information: fmt.Sprintf(
					"There are unknown types on the map: %s\n"+
						"Types below will be discarded on save:\n"+
						"%s", dmm.Name, prefabsNames,
				),
			})
		}
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
