package pmap

import "github.com/SpaiR/imgui-go"

var (
	bottomPanelSize imgui.Vec2
)

const (
	panelPadding  float32 = 5
	panelAlpha    float32 = .75
	panelRounding float32 = 1

	panelFlags = imgui.WindowFlagsNoResize | imgui.WindowFlagsAlwaysAutoResize |
		imgui.WindowFlagsNoTitleBar | imgui.WindowFlagsNoMove
)

type panelPos int

const (
	pPosTop panelPos = iota
	pPosBottom
)

func (p *PaneMap) showPanel(id string, panelPos panelPos, content func()) {
	var pos, size imgui.Vec2

	switch panelPos {
	case pPosTop:
		pos = p.panePos.Plus(imgui.Vec2{X: panelPadding, Y: panelPadding})
		size = imgui.Vec2{X: p.paneSize.X - panelPadding*2}
	case pPosBottom:
		pos = p.panePos.Plus(imgui.Vec2{X: panelPadding, Y: p.paneSize.Y - bottomPanelSize.Y - panelPadding})
		size = imgui.Vec2{X: p.paneSize.X - panelPadding*2}
	}

	imgui.SetNextWindowPos(pos)
	imgui.SetNextWindowSize(size)
	imgui.SetNextWindowBgAlpha(panelAlpha)
	imgui.PushStyleVarFloat(imgui.StyleVarWindowRounding, panelRounding)

	if imgui.BeginV(id, nil, panelFlags) {
		imgui.PopStyleVar()

		content()

		if panelPos == pPosBottom {
			bottomPanelSize = imgui.WindowSize()
		}
	} else {
		imgui.PopStyleVar()
	}
	imgui.End()
}
