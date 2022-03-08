package wschangelog

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/window"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/markdown"
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
	"sdmm/rsc"
)

var (
	parsedChangelog markdown.Markdown
)

type App interface {
	PointSize() float32

	DoOpenSourceCode()
	DoOpenSupport()
}

type WsChangelog struct {
	workspace.Content

	app App
}

func New(app App) *WsChangelog {
	return &WsChangelog{
		app: app,
	}
}

func (ws *WsChangelog) Name() string {
	return icon.ClipboardMultiple + " Changelog"
}

func (ws *WsChangelog) Title() string {
	return "Changelog"
}

func (ws *WsChangelog) Process() {
	ws.showContent()
}

func (ws *WsChangelog) showContent() {
	if parsedChangelog.IsEmpty() {
		parsedChangelog = markdown.Parse(rsc.ChangelogMd)
	}

	logoSize := 100 * ws.app.PointSize()

	w.Layout{
		w.Image(imgui.TextureID(window.AppLogoTexture), logoSize, logoSize),
		w.SameLine(),
		w.Group{
			w.Custom(func() {
				markdown.ShowHeader("StrongDMM Changelog", window.FontH3)
			}),
			w.Separator(),
			w.TextWrapped(rsc.ChangelogHeaderTxt),
			w.NewLine(),
			w.Button("Open Source Code", ws.app.DoOpenSourceCode).
				Icon(icon.GitHub),
			w.SameLine(),
			w.Dummy(imgui.Vec2{}),
			w.SameLine(),
			w.Button("Support the Project", ws.app.DoOpenSupport).
				Style(style.ButtonFireCoral{}).
				Tooltip(rsc.SupportTxt).
				Icon(icon.KoFi),
		},
	}.Build()

	imgui.NewLine()
	markdown.Show(parsedChangelog)
}
