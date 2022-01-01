package wsmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"log"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/wsmap/pmap"
	"sdmm/dmapi/dmmap"
	"sdmm/dmapi/dmmsave"
)

type App interface {
	pmap.App

	CommandStorage() *command.Storage
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

func (ws *WsMap) Save() {
	log.Println("[wsmap] saving map workspace:", ws.CommandStackId())
	dmmsave.Save(ws.paneMap.Dmm())
	ws.app.CommandStorage().ForceBalance(ws.CommandStackId())
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
