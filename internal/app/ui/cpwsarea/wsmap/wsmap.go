package wsmap

import (
	"fmt"
	"os"
	"path/filepath"
	"runtime"
	"sdmm/internal/app/command"
	"sdmm/internal/app/prefs"
	"sdmm/internal/app/ui/cpwsarea/workspace"
	"sdmm/internal/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/internal/dmapi/dmenv"
	"sdmm/internal/dmapi/dmmap"
	"strconv"
	"strings"

	"github.com/SpaiR/imgui-go"
	"github.com/rs/zerolog/log"
)

type App interface {
	pmap.App

	LoadedEnvironment() *dmenv.Dme
	CommandStorage() *command.Storage
	Prefs() prefs.Prefs
}

type WsMap struct {
	workspace.Content

	app App

	paneMap        *pmap.PaneMap
	lastCamX       float32
	lastCamY       float32
	lastCamLevel   int
	lastCamScale   float32
	lastFileString string
}

func New(app App, dmm *dmmap.Dmm) *WsMap {
	return &WsMap{
		app:     app,
		paneMap: pmap.New(app, dmm),
	}
}

func (ws *WsMap) Map() *pmap.PaneMap {
	return ws.paneMap
}

func (ws *WsMap) CommandStackId() string {
	return ws.paneMap.Dmm().Path.Absolute
}

func (WsMap) Ini() workspace.Ini {
	return workspace.Ini{
		WindowFlags: imgui.WindowFlagsNoScrollbar | imgui.WindowFlagsNoBringToFrontOnFocus,
		NoPadding:   true,
	}
}

func (ws *WsMap) Name() string {
	visibleName := ws.paneMap.Dmm().Name
	if ws.app.CommandStorage().IsModified(ws.CommandStackId()) {
		visibleName = "* " + visibleName
	}
	return fmt.Sprint(visibleName, "###workspace_map_", ws.paneMap.Dmm().Path.Absolute)
}

func (ws *WsMap) Title() string {
	return ws.paneMap.Dmm().Name
}

func (ws *WsMap) NameReadable() string {
	return ws.paneMap.Dmm().Name
}

func (ws *WsMap) PreProcess() {
	ws.paneMap.SetShortcutsVisible(false)
	ws.processCanvasCameraMirror()
}

func (ws *WsMap) Process() {
	ws.paneMap.Process()
}

func (ws *WsMap) Dispose() {
	ws.paneMap.Dispose()
	log.Print("map workspace disposed:", ws.Name())
}

func (ws *WsMap) Focused() bool {
	return ws.paneMap.Focused()
}

func (ws *WsMap) OnFocusChange(focused bool) {
	if focused {
		ws.paneMap.OnActivate()
	} else {
		ws.paneMap.OnDeactivate()
	}
}

func (ws *WsMap) processCanvasCameraMirror() {
	if !pmap.MirrorCanvasCamera || pmap.ActiveCamera() == nil {
		return
	}

	activeCamera := pmap.ActiveCamera()
	if camera := ws.paneMap.Canvas().Render().Camera; camera != activeCamera {
		camera.ShiftX = activeCamera.ShiftX
		camera.ShiftY = activeCamera.ShiftY
		camera.Level = activeCamera.Level
		camera.Scale = activeCamera.Scale
	}

	if ws.app.Prefs().Editor.MirrorCanvasCrossApp != true {
		return
	}

	localMoved := activeCamera.ShiftX != ws.lastCamX ||
		activeCamera.ShiftY != ws.lastCamY ||
		activeCamera.Level != ws.lastCamLevel ||
		activeCamera.Scale != ws.lastCamScale

	//let local changes apply
	if activeCamera.Level != ws.lastCamLevel {
		ws.paneMap.SetActiveLevel(max(1, min(activeCamera.Level, ws.paneMap.Dmm().MaxZ)))
	}

	var internalDir string

	userHomeDir, err := os.UserHomeDir()
	if err != nil {
		panic("unable to find user home dir")
	}

	if runtime.GOOS == "windows" {
		internalDir = userHomeDir + "/AppData/Roaming/StrongDMM"
	} else {
		internalDir = userHomeDir + "/.strongdmm"
	}
	_ = os.MkdirAll(internalDir, os.ModePerm)

	coordsPath := filepath.Join(internalDir, "coords.txt")

	if localMoved {
		dataStr := fmt.Sprintf("%f,%f,%d,%f", activeCamera.ShiftX, activeCamera.ShiftY, activeCamera.Level, activeCamera.Scale)
		if dataStr != ws.lastFileString {
			os.WriteFile(coordsPath, []byte(dataStr), 0660)
			ws.lastFileString = dataStr
		}
		ws.lastCamX = activeCamera.ShiftX
		ws.lastCamY = activeCamera.ShiftY
		ws.lastCamLevel = activeCamera.Level
		ws.lastCamScale = activeCamera.Scale

	} else {
		fileData, error := os.ReadFile(coordsPath)
		if error != nil {
			return
		}

		fileStr := strings.TrimSpace(string(fileData))
		if fileStr != ws.lastFileString {
			parts := strings.Split(fileStr, ",")
			if len(parts) == 4 {
				shiftX, _ := strconv.ParseFloat(parts[0], 32)
				shiftY, _ := strconv.ParseFloat(parts[1], 32)
				level, _ := strconv.Atoi(parts[2])
				scale, _ := strconv.ParseFloat(parts[3], 32)

				activeCamera.ShiftX = float32(shiftX)
				activeCamera.ShiftY = float32(shiftY)
				activeCamera.Level = level
				ws.paneMap.SetActiveLevel((max(1, min(level, ws.paneMap.Dmm().MaxZ))))

				activeCamera.Scale = float32(scale)

				if camera := ws.paneMap.Canvas().Render().Camera; camera != activeCamera {
					camera.ShiftX = activeCamera.ShiftX
					camera.ShiftY = activeCamera.ShiftY
					camera.Level = activeCamera.Level
					camera.Scale = activeCamera.Scale
				}
				ws.paneMap.Canvas().Render().SetActiveLevel(ws.paneMap.Dmm(), (max(1, min(level, ws.paneMap.Dmm().MaxZ))))
				ws.lastCamX = activeCamera.ShiftX
				ws.lastCamY = activeCamera.ShiftY
				ws.lastCamLevel = activeCamera.Level
				ws.lastCamScale = activeCamera.Scale
				ws.lastFileString = fileStr
			}
		}

	}

}
