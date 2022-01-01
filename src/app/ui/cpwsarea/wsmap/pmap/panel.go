package pmap

import "github.com/SpaiR/imgui-go"

const (
	panelPadding  float32 = 5
	panelAlpha    float32 = .75
	panelRounding float32 = 1

	panelFlags = imgui.WindowFlagsNoResize | imgui.WindowFlagsAlwaysAutoResize |
		imgui.WindowFlagsNoTitleBar | imgui.WindowFlagsNoMove |
		imgui.WindowFlagsNoSavedSettings | imgui.WindowFlagsNoDocking
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
		pos = p.pos.Plus(imgui.Vec2{X: panelPadding, Y: panelPadding})
		size = imgui.Vec2{X: p.size.X - panelPadding*2}
	case pPosBottom:
		pos = p.pos.Plus(imgui.Vec2{X: panelPadding, Y: imgui.ContentRegionAvail().Y - p.panelBottomSize.Y - panelPadding})
		size = imgui.Vec2{X: p.size.X - panelPadding*2}
	}

	imgui.SetNextWindowPos(pos)
	imgui.SetNextWindowSize(size)
	imgui.SetNextWindowBgAlpha(panelAlpha)
	imgui.PushStyleVarFloat(imgui.StyleVarWindowRounding, panelRounding)

	if imgui.BeginV(id, nil, panelFlags) {
		imgui.PopStyleVar()

		p.updateShortcutsState()
		p.focused = p.focused || imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)

		content()

		if panelPos == pPosBottom {
			p.panelBottomSize = imgui.WindowSize()
		}
	} else {
		imgui.PopStyleVar()
	}
	imgui.End()
}
