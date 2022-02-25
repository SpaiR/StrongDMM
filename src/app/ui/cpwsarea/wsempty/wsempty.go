package wsempty

import (
	"fmt"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/dmapi/dmenv"
	"strings"

	"github.com/SpaiR/imgui-go"
)

type App interface {
	DoOpenEnvironment()
	DoOpenEnvironmentByPath(path string)

	DoOpenCreateMap()
	DoSelectMapFile() (string, error)

	HasLoadedEnvironment() bool
	LoadedEnvironment() *dmenv.Dme

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string
}

type WsEmpty struct {
	workspace.Content

	app App

	onOpenMapByPath func(string)
}

func New(app App) *WsEmpty {
	return &WsEmpty{app: app}
}

func (ws *WsEmpty) Name() string {
	return "Workspace"
}

func (ws *WsEmpty) Title() string {
	return "Workspace"
}

func (ws *WsEmpty) SetOnOpenMapByPath(f func(string)) {
	ws.onOpenMapByPath = f
}

func (ws *WsEmpty) Process() {
	ws.showContent()
}

func (ws *WsEmpty) showContent() {
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
	if imgui.Button("New Map...") {
		ws.app.DoOpenCreateMap()
	}
	if imgui.Button("Open Map...") {
		if file, err := ws.app.DoSelectMapFile(); err == nil {
			ws.onOpenMapByPath(file)
		}
	}
	imgui.Separator()
	if len(ws.app.RecentMapsByLoadedEnvironment()) == 0 {
		imgui.Text("No Recent Maps")
	} else {
		imgui.Text("Recent Maps:")
		for _, mapPath := range ws.app.RecentMapsByLoadedEnvironment() {
			if imgui.SmallButton(sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, mapPath)) {
				ws.onOpenMapByPath(mapPath)
			}
		}
	}
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
