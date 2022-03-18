package psettings

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/wsmap/pmap/editor"
	"sdmm/app/window"
)

type Panel struct {
	editor *editor.Editor

	sessionMapSize *sessionMapSize
}

func New(editor *editor.Editor) *Panel {
	return &Panel{editor: editor}
}

func (p *Panel) Process() {
	imgui.Dummy(imgui.Vec2{X: p.headerSize()})
	p.showMapSize()
}

func (p *Panel) headerSize() float32 {
	return window.PointSize() * 150
}
