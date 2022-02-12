package cpprefabs

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	w "sdmm/imguiext/widget"
)

func (p *Prefabs) Process() {
	if len(p.nodes) == 0 {
		imgui.TextDisabled("No Prefab Selected")
		return
	}

	for _, node := range p.nodes {
		isSelected := node.orig.Id() == p.selectedId

		if imgui.SelectableV(
			fmt.Sprintf("##prefab_%d", node.orig.Id()),
			isSelected,
			imgui.SelectableFlagsNone,
			imgui.Vec2{Y: p.iconSize()},
		) {
			p.doSelect(node)
		}

		if isSelected && p.tmpDoScrollToPrefab {
			imgui.SetScrollHereY(.5)
			p.tmpDoScrollToPrefab = false
		}

		p.showContextMenu(node)

		imgui.SameLine()
		imgui.IndentV(p.textIndent())
		imgui.Text(node.name)
		imgui.UnindentV(p.textIndent())

		imgui.SameLine()
		imgui.IndentV(p.iconIndent())
		w.Image(imgui.TextureID(node.sprite.Texture()), p.iconSize(), p.iconSize()).
			Uv(
				imgui.Vec2{
					X: node.sprite.U1,
					Y: node.sprite.V1,
				},
				imgui.Vec2{
					X: node.sprite.U2,
					Y: node.sprite.V2,
				},
			).
			TintColor(node.color).
			Build()
		imgui.UnindentV(p.iconIndent())
	}
}
