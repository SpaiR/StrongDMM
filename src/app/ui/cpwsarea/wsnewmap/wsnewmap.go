package wsnewmap

import (
	"log"
	"math"
	"path/filepath"
	"sdmm/app/command"
	"sdmm/app/prefs"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/window"
	"sdmm/dmapi/dmenv"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/markdown"

	"github.com/SpaiR/imgui-go"
	"github.com/sqweek/dialog"
)

type App interface {
	LoadedEnvironment() *dmenv.Dme

	CommandStorage() *command.Storage
	Prefs() prefs.Prefs

	DoOpenMapByPath(path string)
	DoClose()
}

type WsNewMap struct {
	workspace.Content

	app App

	mapWidth  int // x
	mapHeight int // y
	mapZDepth int // z

	multiZType string

	newMapMetaFile string
}

func New(app App) *WsNewMap {
	return &WsNewMap{
		app: app,

		mapWidth:       1,
		mapHeight:      1,
		mapZDepth:      1,
		multiZType:     Default,
		newMapMetaFile: "",
	}
}

func (ws *WsNewMap) Name() string {
	return icon.FaPlus + " New Map"
}

func (ws *WsNewMap) Title() string {
	return "New Map"
}

func (ws *WsNewMap) Process() {
	ws.showContent()
}

func (ws *WsNewMap) showContent() {
	// Header
	markdown.ShowHeader("Create a new Map", window.FontH2)
	imgui.Separator()
	imgui.NewLine()

	// Width Height
	markdown.ShowHeader("Width (X)", window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled("The width of the map. Minimum is 1.")
	imgui.PopTextWrapPos()

	vwidth := int32(ws.mapWidth)
	if imguiext.InputIntClamp("##map_width", &vwidth, 1, math.MaxInt32, 1, 1) {
		if int(vwidth) != ws.mapWidth {
			ws.mapWidth = int(vwidth)
		}
	}
	imgui.NewLine()

	markdown.ShowHeader("Height (Y)", window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled("The height of the map. Minimum is 1.")
	imgui.PopTextWrapPos()

	vheight := int32(ws.mapHeight)
	if imguiext.InputIntClamp("##map_height", &vheight, 1, math.MaxInt32, 1, 1) {
		if int(vheight) != ws.mapHeight {
			ws.mapHeight = int(vheight)
		}
	}
	imgui.NewLine()

	markdown.ShowHeader("Z Levels", window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled("Z-Height of the map.")
	imgui.PopTextWrapPos()

	vzdepth := int32(ws.mapZDepth)
	if imguiext.InputIntClamp("##map_zheight", &vzdepth, 1, math.MaxInt32, 1, 1) {
		if int(vzdepth) != ws.mapZDepth {
			ws.mapZDepth = int(vzdepth)
		}
	}
	imgui.NewLine()

	// MultiZ: Select types
	markdown.ShowHeader("MultiZ Type", window.FontH3)

	imgui.PushTextWrapPos()
	imgui.TextDisabled("Select what kind of MultiZ you want.")
	imgui.SameLine()
	imgui.TextDisabled(icon.FaQuestionCircle)
	imguiext.SetItemHoveredTooltip("Default - Single file containing multiple z levels.\nMulti File Z Level - Allows tg-like multi file z level creation (Not Implemented!!).")
	imgui.PopTextWrapPos()

	if imgui.BeginCombo("##map_multiztype", ws.multiZType) {
		for _, option := range ZTypes {
			if option == "Multi File Z Level" {
				// not implemented
				continue
			}
			if imgui.SelectableV(option, option == ws.multiZType, imgui.SelectableFlagsNone, imgui.Vec2{}) {
				ws.multiZType = option
			}
		}
		imgui.EndCombo()
	}
	imgui.NewLine()

	// MultiZ: Multiple file using tg json map format (TO BE IMPLEMENTED)
	// if ws.multiZType == MultiFile_Z {
	// 	markdown.ShowHeader("Save location (.json)", window.FontH3)

	// 	imgui.PushTextWrapPos()
	// 	imgui.TextDisabled("Where should this save the multifile meta information.")
	// 	imgui.PopTextWrapPos()
	// 	if imgui.Button("Save as...") {
	// 		// this doesnt create a file
	// 	}

	// 	imgui.NewLine()
	// }

	imgui.Separator()
	markdown.ShowHeader("Create a new map!", window.FontH3)

	if imgui.Button("Save as...") {
		ws.tryCreateMap()
	}
}

func (ws *WsNewMap) dmmSaveLocation() (string, error) {
	log.Println("[wsnewmap] saving map file...")
	return dialog.
		File().
		Title("Save Map").
		Filter(".dmm").
		SetStartDir(ws.app.LoadedEnvironment().RootDir).
		Save()
}

func (ws *WsNewMap) tryCreateMap() {
	log.Println("[wsnewmap] trying to create a new map with X:", ws.mapWidth, "| Y:", ws.mapHeight, "| Z:", ws.mapZDepth)

	// width height cannot be invalid surely <- clueless
	if ws.multiZType == MultiFile_Z && len(ws.newMapMetaFile) > 0 {
		log.Println("[wsnewmap] map meta path is nil or blank")
		// dialog.Open(dialog.type{
		//  idk how to make this not clash
		// })
		return
	}

	// save
	if file, err := ws.dmmSaveLocation(); err == nil {
		if filepath.Ext(file) != ".dmm" {
			file = file + ".dmm"
		}
		log.Println("[wsnewmap] saving map to:", file)
		if ws.SaveNewMap(file) {
			// open
			ws.app.DoOpenMapByPath(file)
			// close us
			ws.app.DoClose()
			return
		}
	}
}
