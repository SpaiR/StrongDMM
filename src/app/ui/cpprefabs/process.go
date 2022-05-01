package cpprefabs

import (
	"fmt"

	w "sdmm/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

func (p *Prefabs) Process() {
	if len(p.nodes) == 0 {
		imgui.TextDisabled("No Prefab Selected")
		return
	}

	for _, node := range p.nodes {
		isSelected := node.orig.Id() == p.selectedId
		cursor := imgui.CursorPos()

		if isSelected && p.tmpDoScrollToPrefab {
			imgui.SetScrollHereY(.5)
			p.tmpDoScrollToPrefab = false
		}

		if node.visHeight != 0 {
			if imgui.SelectableV(
				fmt.Sprintf("##prefab_%d", node.orig.Id()),
				isSelected,
				imgui.SelectableFlagsNone,
				imgui.Vec2{Y: node.visHeight},
			) {
				p.doSelect(node)
			}

			p.showContextMenu(node)
		}

		imgui.SetCursorPos(cursor)

		imgui.BeginGroup()
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

		imgui.SameLine()

		imgui.BeginGroup()
		imgui.Text(node.name)
		imgui.EndGroup()

		imgui.EndGroup()

		node.visHeight = imgui.ItemRectMax().Y - imgui.ItemRectMin().Y
	}
}
