package wsempty

import (
	"strings"

	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

type App interface {
	DoOpenV(*workspace.Workspace)

	DoLoadResource(string)
	DoLoadResourceV(string, *workspace.Workspace)

	DoClearRecentEnvironments()
	DoClearRecentMaps()

	DoRemoveRecentEnvironment(string)
	DoRemoveRecentMap(string)
	DoRemoveRecentMaps([]string)

	HasLoadedEnvironment() bool
	LoadedEnvironment() *dmenv.Dme

	RecentEnvironments() []string
	RecentMapsByLoadedEnvironment() []string
	RecentMaps() []string

	AvailableMaps() []string
}

type WsEmpty struct {
	workspace.Content

	app App

	filter string

	availableMaps []string
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

func (ws *WsEmpty) Process() {
	ws.showContent()
}

func (ws *WsEmpty) showContent() {
	if !ws.app.HasLoadedEnvironment() {
		ws.showNoEnvControls()
	} else {
		ws.showNoMapControls()
	}
}

func (ws *WsEmpty) showNoEnvControls() {
	ws.showOpenButton("environment (.dme) or map (.dmm) file to proceed")
	imgui.NewLine()
	ws.showFilter()
	imgui.NewLine()

	recentEnvs := ws.app.RecentEnvironments()

	showHeaderRecent(len(recentEnvs) == 0, "Recent Environments", ws.app.DoClearRecentEnvironments)

	if len(recentEnvs) == 0 {
		imgui.TextDisabled("No recent environments")
	}

	for _, recentEnvironment := range recentEnvs {
		if !strings.Contains(strings.ToLower(recentEnvironment), strings.ToLower(ws.filter)) {
			continue
		}

		showRecent(recentEnvironment, func() {
			ws.app.DoRemoveRecentEnvironment(recentEnvironment)
		}, func() {
			ws.app.DoLoadResource(recentEnvironment)
		})
	}

	imgui.NewLine()

	recentMaps := ws.app.RecentMaps()

	showHeaderRecent(len(recentMaps) == 0, "Recent Maps", ws.app.DoClearRecentMaps)

	if len(recentMaps) == 0 {
		imgui.TextDisabled("No recent maps")
	}

	for _, recentMap := range recentMaps {
		if !strings.Contains(strings.ToLower(recentMap), strings.ToLower(ws.filter)) {
			continue
		}

		showRecent(recentMap, func() {
			ws.app.DoRemoveRecentMap(recentMap)
		}, func() {
			ws.app.DoLoadResourceV(recentMap, ws.Root())
		})
	}
}

func (ws *WsEmpty) showNoMapControls() {
	ws.showOpenButton("map (.dmm) file to proceed")
	imgui.NewLine()
	ws.showFilter()
	imgui.NewLine()

	recentMaps := ws.app.RecentMapsByLoadedEnvironment()

	showHeaderRecent(len(recentMaps) == 0, "Recent Maps", func() {
		ws.app.DoRemoveRecentMaps(recentMaps)
	})

	if len(recentMaps) == 0 {
		imgui.TextDisabled("No recent maps")
	}

	for _, recentMap := range recentMaps {
		if !strings.Contains(strings.ToLower(recentMap), strings.ToLower(ws.filter)) {
			continue
		}

		label := sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, recentMap)
		showRecent(label, func() {
			ws.app.DoRemoveRecentMap(recentMap)
		}, func() {
			ws.app.DoLoadResourceV(recentMap, ws.Root())
		})
	}

	imgui.NewLine()

	if ws.availableMaps == nil {
		ws.availableMaps = ws.app.AvailableMaps()
	}

	if availableMaps := ws.availableMaps; len(availableMaps) != 0 {
		w.Layout{
			w.TextColored("Available Maps", style.ColorGold),
			w.Separator(),
		}.Build()

		if imgui.BeginChild("available_maps") {
			for _, recentMap := range availableMaps {
				if !strings.Contains(strings.ToLower(recentMap), strings.ToLower(ws.filter)) {
					continue
				}
				if imgui.Selectable(sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, recentMap)) {
					ws.app.DoLoadResourceV(recentMap, ws.Root())
				}
			}
			imgui.EndChild()
		}
	}
}

func (ws *WsEmpty) showOpenButton(help string) {
	w.Layout{
		w.AlignTextToFramePadding(),
		w.Button("Open...", func() {
			ws.app.DoOpenV(ws.Root())
		}).Style(style.ButtonGreen{}),
		w.SameLine(),
		w.TextDisabled(help),
	}.Build()
}

func (ws *WsEmpty) showFilter() {
	w.InputTextWithHint("##filter", "Filter", &ws.filter).ButtonClear().Build()
}

func showHeaderRecent(disabled bool, label string, action func()) {
	w.Layout{
		w.Disabled(disabled,
			w.Button(icon.Delete+"###"+label, action).
				Small(true).
				Tooltip("Clear").
				Style(style.ButtonRed{})),
		w.SameLine(),
		w.TextColored(label, style.ColorGold),
		w.Separator(),
	}.Build()
}

func showRecent(label string, removeAction, openAction func()) {
	w.Layout{
		w.Button(icon.Clear+"###"+label, removeAction).
			Tooltip("Remove").
			Small(true),
		w.SameLine(),
		w.Button(label, openAction).
			Small(true),
	}.Build()
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
