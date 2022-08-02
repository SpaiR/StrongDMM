package pmap

import (
	"fmt"

	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
	"sdmm/imguiext/icon"
	w "sdmm/imguiext/widget"
	"sdmm/platform"
)

func (p *PaneMap) showStatusPanel() {
	w.Layout{
		p.panelStatusLayoutStatus(),
		w.SameLine(),
		w.Custom(func() {
			if p.dmm.MaxZ != 1 {
				w.Layout{
					p.panelStatusLayoutLevels(),
				}.BuildV(w.AlignRight)
			}
		}),
	}.Build()
}

func (p *PaneMap) panelStatusLayoutStatus() (layout w.Layout) {
	if p.canvasState.HoverOutOfBounds() {
		layout = append(layout, w.TextFrame("out of bounds"))
	} else {
		t := p.canvasState.HoveredTile()
		layout = append(layout, w.TextFrame(fmt.Sprintf("X:%03d Y:%03d", t.X, t.Y)))
	}

	layout = append(layout, w.Tooltip(w.Text("Tile coordinates of the mouse")))

	if isQuickToolToggled() && !tools.Selected().AltBehaviour() {
		if hoveredInstance := p.canvasState.HoveredInstance(); hoveredInstance != nil {
			layout = append(layout, w.TextFrame(hoveredInstance.Prefab().Path()))
		}
	} else if tool, ok := tools.Selected().(*tools.ToolGrab); ok && tool.HasSelectedArea() {
		bounds := tool.Bounds()
		layout = append(layout,
			w.TextFrame(fmt.Sprintf("W:%d H:%d", int(bounds.X2-bounds.X1)+1, int(bounds.Y2-bounds.Y1)+1)),
			w.Tooltip(w.Text("Grab area size")),
			w.TextFrame(bounds.String()),
			w.Tooltip(w.Text("Grab area bounds")),
		)
	}

	return w.Layout{
		w.Line(layout...),
	}
}

func isQuickToolToggled() bool {
	return tools.IsSelected(tools.TNPick) || tools.IsSelected(tools.TNDelete) || tools.IsSelected(tools.TNReplace)
}

func (p *PaneMap) panelStatusLayoutLevels() (layout w.Layout) {
	return w.Layout{
		w.TextFrame(fmt.Sprintf("Z:%d", p.activeLevel)),
		w.Tooltip(w.Text("Current Z-level")).OnHover(true),
		w.SameLine(),
		w.Disabled(!p.hasPreviousLevel(), w.Layout{
			w.Button(icon.ArrowDownward, p.doPreviousLevel).
				Tooltip(fmt.Sprintf("Previous z-level (%s)", shortcut.Combine(platform.KeyModName(), "Down"))).
				Round(true),
		}),
		w.SameLine(),
		w.Disabled(!p.hasNextLevel(), w.Layout{
			w.Button(icon.ArrowUpward, p.doNextLevel).
				Tooltip(fmt.Sprintf("Next z-level (%s)", shortcut.Combine(platform.KeyModName(), "Up"))).
				Round(true),
		}),
	}
}
