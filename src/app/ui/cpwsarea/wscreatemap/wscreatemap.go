package wscreatemap

import (
	"log"
	"math"
	"path/filepath"

	"sdmm/app/prefs"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/window"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/markdown"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
	"github.com/sqweek/dialog"
)

type App interface {
	LoadedEnvironment() *dmenv.Dme
	Prefs() prefs.Prefs
	FocusApplicationWindow()
	DoLoadResourceV(string, *workspace.Workspace)
}

type WsCreateMap struct {
	workspace.Content

	app App

	mapWidth  int // x
	mapHeight int // y
	mapZDepth int // z

	format string
}

func New(app App) *WsCreateMap {
	format := app.Prefs().Editor.SaveFormat
	if format == prefs.SaveFormatInitial { // Enforce TGM usage.
		format = prefs.SaveFormatTGM
	}

	return &WsCreateMap{
		app: app,

		mapWidth:  1,
		mapHeight: 1,
		mapZDepth: 1,

		format: format,
	}
}

func (ws *WsCreateMap) Name() string {
	return "Create Map"
}

func (ws *WsCreateMap) Title() string {
	return ws.Name()
}

func (ws *WsCreateMap) Process() {
	ws.showInput("Width (X)", "The width of the map in tiles.", &ws.mapWidth)
	imgui.NewLine()
	ws.showInput("Height (Y)", "The height of the map in tiles.", &ws.mapHeight)
	imgui.NewLine()
	ws.showInput("Levels (Z)", "Z-Height of the map.", &ws.mapZDepth)
	imgui.NewLine()
	ws.showFormatButton()
	imgui.NewLine()
	ws.showSaveButton()
}

func (ws *WsCreateMap) showInput(label, desc string, value *int) {
	markdown.ShowHeader(label, window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled(desc)
	imgui.PopTextWrapPos()

	v := int32(*value)
	if imguiext.InputIntClamp("##"+label, &v, 1, math.MaxInt32, 1, 25) {
		*value = int(v)
	}
}

func (ws *WsCreateMap) showFormatButton() {
	markdown.ShowHeader("Format", window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled("A format to save the map.")
	imgui.SameLine()
	imgui.TextDisabled(icon.Help)
	imguiext.SetItemHoveredTooltip(`TGM - a custom map format made by TG, helps to make map file more readable and reduce merge conflicts
DM - a default map format used by the DM map editor`)
	imgui.PopTextWrapPos()

	if imgui.BeginCombo("##format", ws.format) {
		if imgui.SelectableV(prefs.SaveFormatTGM, prefs.SaveFormatTGM == ws.format, imgui.SelectableFlagsNone, imgui.Vec2{}) {
			ws.format = prefs.SaveFormatTGM
		}
		if imgui.SelectableV(prefs.SaveFormatDMM, prefs.SaveFormatDMM == ws.format, imgui.SelectableFlagsNone, imgui.Vec2{}) {
			ws.format = prefs.SaveFormatDMM
		}
		imgui.EndCombo()
	}
}

func (ws *WsCreateMap) showSaveButton() {
	w.Button("Create...", ws.tryCreateMap).
		Style(style.ButtonGreen{}).
		Icon(icon.Save).
		Build()
}

func (ws *WsCreateMap) dmmSaveLocation() (string, error) {
	log.Println("[wsnewmap] creating map file...")
	return dialog.
		File().
		Title("New Map").
		Filter(".dmm").
		SetStartDir(ws.app.LoadedEnvironment().RootDir).
		Save()
}

func (ws *WsCreateMap) tryCreateMap() {
	log.Println("[wsnewmap] trying to create a new map with X:", ws.mapWidth, "| Y:", ws.mapHeight, "| Z:", ws.mapZDepth)

	if file, err := ws.dmmSaveLocation(); err == nil {
		ws.app.FocusApplicationWindow() // After a system dialog has been opened we need to return the focus.

		if filepath.Ext(file) != ".dmm" {
			file = file + ".dmm"
		}

		log.Println("[wsnewmap] saving new map to:", file)

		ws.save(file)
		ws.app.DoLoadResourceV(file, ws.Root())
	} else {
		log.Println("[wsnewmap] unable to get new map save location:", err)
	}
}
