package app

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sdmm/third_party/sdmmparser"
	"time"

	"sdmm/internal/app/ui/cpwsarea/workspace"
	"sdmm/internal/app/ui/dialog"
	"sdmm/internal/app/window"
	"sdmm/internal/dmapi/dm"
	"sdmm/internal/imguiext/style"
	w "sdmm/internal/imguiext/widget"
	"sdmm/internal/util/slice"

	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmicon"
	"sdmm/internal/dmapi/dmmap"
	"sdmm/internal/dmapi/dmmap/dmmdata"
	"sdmm/internal/util"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
)

func (a *app) loadResource(path string) {
	a.loadResourceV(path, nil)
}

// Universal method to open any editor resource.
// If it gets a map file, then the code will try to find an environment to open it.
func (a *app) loadResourceV(path string, ws *workspace.Workspace) {
	path, err := filepath.Abs(path)
	if err != nil {
		log.Print("unable to get resource absolute path:", err)
		return
	}

	if filepath.Ext(path) == ".dme" {
		a.loadEnvironment(path)
		return
	}

	if filepath.Ext(path) != ".dmm" {
		log.Print("invalid resource to load:", path)
		return
	}

	environmentPath, err := findEnvironmentFileFromBase(path)

	if a.HasLoadedEnvironment() {
		a.loadMap(path, ws)
		return
	}

	if err == nil {
		a.loadEnvironmentV(environmentPath, func() {
			a.loadMap(path, ws)
		})
	} else {
		log.Print("unable to find environment from file:", path)
		dialog.Open(dialog.TypeInformation{
			Title: "No dme found!",
			Information: "Can't find an environment file.\n" +
				"Please, ensure it can be accessed or open manually.\n" +
				path,
		})
	}
}

// Goes through all parents starting from the current file location and look for a ".dme" file.
func findEnvironmentFileFromBase(path string) (string, error) {
	for {
		dir := filepath.Dir(path)

		if dir == path {
			return "", fmt.Errorf("unable to find environment")
		}

		files, err := os.ReadDir(dir)
		if err != nil {
			log.Print("unable to read dir while looking for environment:", err)
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
	a.closeEnvironment(func(closed bool) {
		if closed {
			a.forceLoadEnvironment(path, callback)
		}
	})
}

func (a *app) forceLoadEnvironment(path string, callback func()) {
	log.Printf("opening environment [%s]...", path)

	afterLoad := func(env *dmenv.Dme) {
		a.freeEnvironmentResources()

		a.projectConfig().AddProject(path)
		a.loadedEnvironment = env
		a.pathsFilter = newPathsFilter(env)

		dmicon.Cache.SetRootDirPath(env.RootDir)
		dmmap.Init(env)

		a.layout.WsArea.AddEmptyWorkspaceIfNone()
		a.UpdateTitle()

		runtime.GC()

		log.Print("environment opened:", path)

		if callback != nil {
			callback()
		}
	}

	go func() {
		dlg := makeLoadingDialog(path)
		dialog.Open(dlg)
		defer dialog.Close(dlg)

		start := time.Now()
		log.Printf("parsing environment: [%s]...", path)

		env, err := dmenv.New(path)

		if err != nil {
			log.Print("unable to open environment by path:", path, err)

			if sdmmparser.IsParserError(err) {
				dialog.Open(dialog.TypeCustom{
					Title:       "Parser Error!",
					CloseButton: true,
					Layout: w.Layout{
						w.Text("Unable to open environment: " + path),
						w.Separator(),
						w.Text(err.Error()),
					},
				})
			} else {
				dialog.Open(dialog.TypeInformation{
					Title:       "Error!",
					Information: "Unable to open environment: " + path,
				})
			}
			return
		}

		log.Printf("environment [%s] parsed in [%d] ms", path, time.Since(start).Milliseconds())

		window.RunLater(func() {
			afterLoad(env)
		})
	}()
}

func makeLoadingDialog(path string) dialog.Type {
	start := time.Now()
	return dialog.TypeCustom{
		Title: "Loading",
		Layout: w.Layout{
			w.Text(path),
			w.Custom(func() {
				passed := fmt.Sprint(time.Since(start).Round(time.Second))

				width := imgui.WindowWidth()
				textW := imgui.CalcTextSize(passed, false, 0).X

				imgui.SetCursorPos(imgui.Vec2{X: (width - textW) * .5, Y: imgui.CursorPosY()})
				imgui.TextColored(style.ColorGold, passed)
			}),
		},
	}
}

// Configure paths filter to access a newly opened environment.
func newPathsFilter(env *dmenv.Dme) *dm.PathsFilter {
	return dm.NewPathsFilter(func(path string) []string {
		return env.Objects[path].DirectChildren
	})
}

func (a *app) loadMap(path string, workspace *workspace.Workspace) {
	log.Printf("opening map [%s]...", path)

	start := time.Now()
	log.Printf("parsing map: [%s]...", path)
	data, err := dmmdata.New(path)
	if err != nil {
		log.Printf("unable to open map by path [%s]: %v", path, err)
		dialog.Open(dialog.TypeInformation{
			Title:       "Error: Unable to open map",
			Information: fmt.Sprintf("Error while parsing the map:\n - %s\n - %s", path, err),
		})
		return
	}
	elapsed := time.Since(start).Milliseconds()
	log.Printf("map [%s] parsed in [%d] ms", path, elapsed)

	// Add map to the recent only if it is a part of the currently opened environment.
	if slice.StrContains(a.AvailableMaps(), path) {
		log.Print("adding map path to the recent:", path)
		cfg := a.projectConfig()
		cfg.AddMap(path)
	} else {
		log.Print("ignoring map path add to the recent, since it's an outside resource")
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

	log.Print("map opened:", path)
}

func (a *app) closeEnvironment(callback func(bool)) {
	// NewMap workspaces depend on the opened environment, so we close them too.
	a.layout.WsArea.CloseAllCreateMaps()
	a.layout.WsArea.CloseAllMaps(func(closed bool) {
		if callback != nil {
			callback(closed)
		}
	})
}

// Frees all resources connected with opened environment.
func (a *app) freeEnvironmentResources() {
	log.Print("free environment resources...")

	a.pathsFilter = dm.NewPathsFilterEmpty()

	a.layout.Prefabs.Free()
	a.layout.Search.Free()
	a.layout.Environment.Free()
	a.layout.WsArea.Free()
	a.layout.VarEditor.Free()

	a.commandStorage.Free()
	a.clipboard.Free()

	dmicon.Cache.Free()
	dmmap.PrefabStorage.Free()
	dmmap.Free()

	a.loadedEnvironment = nil

	a.UpdateTitle()

	log.Print("environment resources free!")
}

func (a *app) environmentName() string {
	if a.loadedEnvironment != nil {
		return a.loadedEnvironment.Name
	}
	return ""
}

func (a *app) backupMap(path string) string {
	data, err := os.ReadFile(path)
	if err != nil {
		log.Print("unable to read map to backup:", path)
		util.ShowErrorDialog("Unable to read map to backup: " + path)
		os.Exit(1)
	}

	// format: backup/environment.dme/map.dmm/time.dmm
	dst := filepath.FromSlash(a.backupDir + "/" +
		a.environmentName() + "/" +
		filepath.Base(path) + "/" +
		time.Now().Format(util.TimeFormat) + ".dmm",
	)

	_ = os.MkdirAll(filepath.Dir(dst), os.ModePerm)

	err = os.WriteFile(dst, data, os.ModePerm)
	if err != nil {
		log.Print("unable to write map backup to a file:", dst)
		util.ShowErrorDialog("Unable to write map backup to a file: " + path)
		os.Exit(1)
	}
	log.Print("map backup created:", dst)

	return dst
}
