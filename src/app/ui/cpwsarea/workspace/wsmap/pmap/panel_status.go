package pmap

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
)

func (p *PaneMap) showStatusPanel() {
	if p.canvasState.HoverOutOfBounds() {
		imgui.Text("[out of bounds]")
	} else {
		hoveredTiles := p.canvasState.HoveredTile()
		imgui.Text(fmt.Sprintf("[X:%03d Y:%03d]", hoveredTiles.X, hoveredTiles.Y))
	}

	if tools.IsSelected(tools.TNPick) || (tools.IsSelected(tools.TNDelete) && !tools.Selected().AltBehaviour()) {
		if hoveredInstance := p.canvasState.HoveredInstance(); hoveredInstance != nil {
			imgui.SameLine()
			imgui.Text(hoveredInstance.Prefab().Path())
		}
	}
}
