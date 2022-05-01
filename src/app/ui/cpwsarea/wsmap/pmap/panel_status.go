package pmap

import (
	"fmt"

	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
	"sdmm/imguiext/icon"
	w "sdmm/imguiext/widget"
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
			layout = append(layout, w.Layout{
				w.SameLine(),
				w.TextFrame(hoveredInstance.Prefab().Path()),
			})
		}
	}

	return layout
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
				Tooltip(fmt.Sprintf("Previous z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Down"))).
				Round(true),
		}),
		w.SameLine(),
		w.Disabled(!p.hasNextLevel(), w.Layout{
			w.Button(icon.ArrowUpward, p.doNextLevel).
				Tooltip(fmt.Sprintf("Next z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Up"))).
				Round(true),
		}),
	}
}
