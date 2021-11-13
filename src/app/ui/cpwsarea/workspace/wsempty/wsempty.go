package wsempty

import (
	"fmt"
	"strings"
	"time"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/dmapi/dmenv"
)

type App interface {
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)
	DoSelectMapFile() (string, error)
	DoOpenMapByPathV(path string, workspaceIdx int)
	HasLoadedEnvironment() bool
	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string
	LoadedEnvironment() *dmenv.Dme
}

type WsEmpty struct {
	workspace.Base

	app  App
	name string
}

func New(app App) *WsEmpty {
	name := fmt.Sprint("New##workspace_empty_", time.Now().Nanosecond())
	ws := &WsEmpty{app: app, name: name}
	ws.Workspace = ws
	return ws
}

func (ws *WsEmpty) Name() string {
	return ws.name
}

func (ws *WsEmpty) NameReadable() string {
	return "New"
}

func (ws *WsEmpty) ShowContent() {
	if !ws.app.HasLoadedEnvironment() {
		ws.showEnvironmentsControl()
	} else {
		ws.showMapsControl()
	}
}

func (ws *WsEmpty) showEnvironmentsControl() {
	if imgui.Button("Open Environment...") {
		ws.app.DoOpenEnvironment()
	}
	imgui.Separator()
	if len(ws.app.RecentEnvironments()) == 0 {
		imgui.Text("No Recent Environments")
	} else {
		imgui.Text("Recent Environments:")
		for _, envPath := range ws.app.RecentEnvironments() {
			if imgui.SmallButton(envPath) {
				ws.app.DoOpenEnvironmentByPath(envPath)
			}
		}
	}
}

func (ws *WsEmpty) showMapsControl() {
	imgui.Text(fmt.Sprint("Environment: ", ws.app.LoadedEnvironment().RootFile))
	imgui.Separator()
	if imgui.Button("Open Map...") {
		if file, err := ws.app.DoSelectMapFile(); err == nil {
			ws.app.DoOpenMapByPathV(file, ws.Idx())
		}
	}
	imgui.Separator()
	if len(ws.app.RecentMapsByLoadedEnvironment()) == 0 {
		imgui.Text("No Recent Maps")
	} else {
		imgui.Text("Recent Maps:")
		for _, mapPath := range ws.app.RecentMapsByLoadedEnvironment() {
			if imgui.SmallButton(sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, mapPath)) {
				ws.app.DoOpenMapByPathV(mapPath, ws.Idx())
			}
		}
	}
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
