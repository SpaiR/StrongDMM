package pmap

import (
	"fmt"
	"sdmm/app/ui/cpwsarea/wsmap/tools"

	"github.com/SpaiR/imgui-go"
)

func (p *PaneMap) showStatusPanel() {
	if p.canvasState.HoverOutOfBounds() {
		imgui.Text("[out of bounds]")
	} else {
		t := p.canvasState.HoveredTile()
		if p.dmm.MaxZ > 1 {
			imgui.Text(fmt.Sprintf("[X:%03d Y:%03d Z:%d]", t.X, t.Y, t.Z))
		} else {
			imgui.Text(fmt.Sprintf("[X:%03d Y:%03d]", t.X, t.Y))
		}
	}

	if tools.IsSelected(tools.TNPick) || (tools.IsSelected(tools.TNDelete) && !tools.Selected().AltBehaviour()) {
		if hoveredInstance := p.canvasState.HoveredInstance(); hoveredInstance != nil {
			imgui.SameLine()
			imgui.Text(hoveredInstance.Prefab().Path())
		}
	}
}
