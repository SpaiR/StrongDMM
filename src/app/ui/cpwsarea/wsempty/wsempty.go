package wsempty

import (
	"fmt"
	"log"
	"math"
	"strings"

	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/shortcut"
	"sdmm/app/window"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/layout"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
	"sdmm/platform"
	"sdmm/util/slice"

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

var (
	filter   string
	splitter = layout.NewSplitter(
		"splitter",
		window.PointSizePtr(),
		.4,
		false,
		false,
	)
)

type WsEmpty struct {
	workspace.Content

	app App

	shortcuts shortcut.Shortcuts

	availableMaps []string
	selectedMaps  []string

	isDoSelectAllMaps bool
}

func New(app App) *WsEmpty {
	return &WsEmpty{
		app: app,
	}
}

func (ws *WsEmpty) Name() string {
	return "Workspace"
}

func (ws *WsEmpty) Title() string {
	return "Workspace"
}

func (ws *WsEmpty) Initialize() {
	ws.addShortcuts()
}

func (ws *WsEmpty) PreProcess() {
	if ws.app.HasLoadedEnvironment() {
		if ws.availableMaps == nil {
			ws.availableMaps = ws.app.AvailableMaps()
		}
	} else {
		ws.availableMaps = nil
		ws.selectedMaps = nil
	}
}

func (ws *WsEmpty) Process() {
	ws.showContent()
}

func (ws *WsEmpty) PostProcess() {
	ws.isDoSelectAllMaps = false
}

func (ws *WsEmpty) Dispose() {
	ws.shortcuts.Dispose()
}

func (ws *WsEmpty) OnFocusChange(focused bool) {
	ws.shortcuts.SetVisible(focused)
}

func (ws *WsEmpty) showContent() {
	if !ws.app.HasLoadedEnvironment() {
		showOpenButton("environment (.dme) or map (.dmm) file to proceed", func() {
			ws.app.DoOpenV(ws.Root())
		})
		imgui.NewLine()
		showFilter(&filter)
		imgui.NewLine()

		splitter.Draw(
			ws.showRecentEnvironments,
			func() {
				ws.showRecentMaps(ws.app.RecentMaps(), false)
			},
		)
	} else {
		showOpenButton("map (.dmm) file to proceed", func() {
			ws.app.DoOpenV(ws.Root())
		})
		imgui.NewLine()
		showFilter(&filter)
		imgui.NewLine()

		splitter.Draw(
			ws.showAvailableMaps,
			func() {
				ws.showRecentMaps(ws.app.RecentMapsByLoadedEnvironment(), true)
			},
		)
	}
}

func (ws *WsEmpty) showRecentEnvironments() {
	recentEnvs := ws.app.RecentEnvironments()

	showHeaderRecent(len(recentEnvs) == 0, "Recent Environments", ws.app.DoClearRecentEnvironments)

	if len(recentEnvs) == 0 {
		imgui.TextDisabled("No recent environments")
	}

	if imgui.BeginChild("recent_environments") {
		for _, recentEnvironment := range recentEnvs {
			if !strings.Contains(strings.ToLower(recentEnvironment), strings.ToLower(filter)) {
				continue
			}

			showRecent(recentEnvironment, func() {
				ws.app.DoRemoveRecentEnvironment(recentEnvironment)
			}, func() {
				ws.app.DoLoadResource(recentEnvironment)
			})
		}
	}
	imgui.EndChild()
}

func (ws *WsEmpty) showRecentMaps(recentMaps []string, sanitizePath bool) {
	showHeaderRecent(len(recentMaps) == 0, "Recent Maps", func() {
		ws.app.DoRemoveRecentMaps(recentMaps)
	})

	if len(recentMaps) == 0 {
		imgui.TextDisabled("No recent maps")
	}

	if imgui.BeginChild("recent_maps") {
		for _, recentMap := range recentMaps {
			if !strings.Contains(strings.ToLower(recentMap), strings.ToLower(filter)) {
				continue
			}

			var label string
			if sanitizePath {
				label = sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, recentMap)
			} else {
				label = recentMap
			}

			showRecent(label, func() {
				ws.app.DoRemoveRecentMap(recentMap)
			}, func() {
				ws.app.DoLoadResourceV(recentMap, ws.Root())
			})
		}
	}
	imgui.EndChild()
}

func (ws *WsEmpty) showAvailableMaps() {
	if availableMaps := ws.availableMaps; len(availableMaps) != 0 {
		w.Layout{
			w.Button(icon.Help, nil).
				TextColor(imgui.CurrentStyle().Color(imgui.StyleColorTextDisabled)).
				Transparent(true),
			w.Tooltip(availableMapsTooltip()),
			w.SameLine(),
			w.TextColored("Available Maps", style.ColorGold),
			w.SameLine(),
			w.Disabled(len(ws.selectedMaps) == 0, ws.openSelectedMapsBtn()),
			w.Separator(),
		}.Build()

		if imgui.BeginChild("available_maps") {
			for _, availableMap := range availableMaps {
				if !strings.Contains(strings.ToLower(availableMap), strings.ToLower(filter)) {
					continue
				}

				if ws.isDoSelectAllMaps {
					ws.addMapSelection(availableMap)
				}

				w.Selectable(sanitizeMapPath(ws.app.LoadedEnvironment().RootDir, availableMap)).
					OnClick(func() { ws.toggleMapSelection(availableMap) }).
					Selected(slice.StrContains(ws.selectedMaps, availableMap)).
					Build()

				if imgui.IsItemClicked() && imgui.IsMouseDoubleClicked(imgui.MouseButtonLeft) {
					ws.app.DoLoadResourceV(availableMap, ws.Root())
				}
			}
		}
		imgui.EndChild()
	}
}

func (ws *WsEmpty) openSelectedMapsBtn() *w.ButtonWidget {
	var label string
	if len(ws.selectedMaps) == 0 {
		label = "No maps selected"
	} else {
		label = fmt.Sprint("Open: ", len(ws.selectedMaps), "###open_selected_maps")
	}

	return w.Button(label, ws.loadSelectedMaps).
		Tooltip(strings.Join(ws.selectedMaps, "\n")).
		Small(true)
}

func (ws *WsEmpty) loadSelectedMaps() {
	for _, selectedMap := range ws.selectedMaps {
		ws.app.DoLoadResource(selectedMap)
	}
}

func (ws *WsEmpty) selectAllMaps() {
	ws.isDoSelectAllMaps = true
}

func (ws *WsEmpty) dropSelectedMaps() {
	log.Println("[wsempty] dropping selected maps")
	ws.selectedMaps = nil
}

func (ws *WsEmpty) addMapSelection(mapPath string) {
	if !slice.StrContains(ws.selectedMaps, mapPath) {
		ws.selectedMaps = append(ws.selectedMaps, mapPath)
		log.Println("[wsempty] map selection added:", mapPath)
	}
}

func (ws *WsEmpty) toggleMapSelection(mapPath string) {
	if imguiext.IsShiftDown() {
		ws.selectMapRange(mapPath)
	} else if imguiext.IsModDown() {
		if slice.StrContains(ws.selectedMaps, mapPath) {
			ws.selectedMaps = slice.StrRemove(ws.selectedMaps, mapPath)
		} else {
			ws.addMapSelection(mapPath)
		}
	} else {
		ws.selectMapSingle(mapPath)
	}
}

func (ws *WsEmpty) selectMapRange(mapPath string) {
	if len(ws.selectedMaps) == 0 {
		ws.selectMapSingle(mapPath)
		return
	}

	firstSelectedMap := ws.selectedMaps[0]

	firstSelectedMapIdx := slice.StrIndexOf(ws.availableMaps, firstSelectedMap)
	currentSelectedMapIdx := slice.StrIndexOf(ws.availableMaps, mapPath)

	startIdx := int(math.Min(float64(firstSelectedMapIdx), float64(currentSelectedMapIdx)))
	endIdx := int(math.Max(float64(firstSelectedMapIdx), float64(currentSelectedMapIdx)))

	ws.dropSelectedMaps()
	ws.addMapSelection(firstSelectedMap) // To ensure that we will always select maps from the ever first selection.
	for idx := startIdx; idx <= endIdx; idx++ {
		ws.addMapSelection(ws.availableMaps[idx])
	}
}

func (ws *WsEmpty) selectMapSingle(mapPath string) {
	log.Println("[wsempty] selecting map:", mapPath)
	ws.dropSelectedMaps()
	ws.addMapSelection(mapPath)
}

func showOpenButton(help string, action func()) {
	w.Layout{
		w.AlignTextToFramePadding(),
		w.Button("Open...", action).
			Style(style.ButtonGreen{}),
		w.SameLine(),
		w.TextDisabled(help),
	}.Build()
}

func showFilter(filter *string) {
	w.InputTextWithHint("##filter", "Filter", filter).ButtonClear().Width(-1).Build()
}

func showHeaderRecent(disabled bool, label string, action func()) {
	w.Layout{
		w.Disabled(disabled,
			w.Button(icon.Delete+"###clear_"+label, action).
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
		w.Button(icon.Clear+"###remove_"+label, removeAction).
			Transparent(true).
			TextColor(style.ColorRed).
			Mouse(imgui.MouseCursorHand).
			Small(true),
		w.SameLine(),
		w.Selectable(label + "###open_" + label).
			Mouse(imgui.MouseCursorHand).
			OnClick(openAction),
	}.Build()
}

func availableMapsTooltip() w.Layout {
	return w.Layout{
		w.TextFrame("Enter"), w.SameLine(), w.Text("Open"),
		w.TextFrame("Click"), w.SameLine(), w.Text("Select"),
		w.TextFrame("Double Click"), w.SameLine(), w.Text("Open"),
		w.TextFrame("Shift+Click"), w.SameLine(), w.Text("Select Range"),
		w.TextFrame(platform.KeyModName() + "+Click"), w.SameLine(), w.Text("Add/Remove Selection"),
		w.TextFrame(platform.KeyModName() + "+A"), w.SameLine(), w.Text("Select All"),
		w.TextFrame(platform.KeyModName() + "+D (ESC)"), w.SameLine(), w.Text("Deselect"),
	}
}

func sanitizeMapPath(envRootDir, mapPath string) string {
	return strings.Replace(mapPath, envRootDir, "", 1)[1:]
}
