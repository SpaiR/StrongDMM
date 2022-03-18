package wsmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"log"
	"sdmm/app/command"
	"sdmm/app/prefs"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/dmapi/dmenv"
	"sdmm/dmapi/dmmap"
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

	paneMap *pmap.PaneMap
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
	log.Println("[wsmap] map workspace disposed:", ws.Name())
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
}
