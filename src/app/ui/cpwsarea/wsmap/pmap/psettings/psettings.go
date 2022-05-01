package psettings

import (
	"sdmm/app/config"
	"sdmm/app/window"
	"sdmm/dmapi/dm"
	"sdmm/dmapi/dmmap"

	"github.com/SpaiR/imgui-go"
)

type App interface {
	PathsFilter() *dm.PathsFilter

	ConfigRegister(config.Config)
}

type editor interface {
	ActiveLevel() int

	Dmm() *dmmap.Dmm
	CommitMapSizeChange(oldMaxX, oldMaxY, oldMaxZ int)
}

type Panel struct {
	app App

	editor editor

	sessionMapSize    *sessionMapSize
	sessionScreenshot *sessionScreenshot
}

var cfg *psettingsConfig

func New(app App, editor editor) *Panel {
	if cfg == nil {
		cfg = loadConfig(app)
	}
	return &Panel{app: app, editor: editor, sessionScreenshot: &sessionScreenshot{}}
}

func (p *Panel) Process() {
	imgui.Dummy(imgui.Vec2{X: p.headerSize()})
	p.showMapSize()
	p.showScreenshot()
}

func (p *Panel) headerSize() float32 {
	return window.PointSize() * 150
}
