package wschangelog

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/imguiext/icon"
	"sdmm/rsc"
)

type WsChangelog struct {
	workspace.Content
}

func New() *WsChangelog {
	return &WsChangelog{}
}

func (ws *WsChangelog) Name() string {
	return icon.FaClipboardList + " Changelog"
}

func (ws *WsChangelog) Title() string {
	return "Changelog"
}

func (ws *WsChangelog) Process() {
	ws.showContent()
}

func (ws *WsChangelog) showContent() {
	imgui.Text(rsc.Changelog)
}
