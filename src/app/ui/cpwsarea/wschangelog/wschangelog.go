package wschangelog

import (
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/markdown"
	"sdmm/rsc"
)

var (
	parsedChangelog markdown.Markdown
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
	if parsedChangelog.IsEmpty() {
		parsedChangelog = markdown.Parse(rsc.Changelog)
	}
	markdown.Show(parsedChangelog)
}
