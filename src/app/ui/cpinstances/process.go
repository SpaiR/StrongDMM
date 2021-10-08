package cpinstances

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	w "sdmm/imguiext/widget"
)

func (i *Instances) Process() {
	for _, node := range i.instanceNodes {
		isSelected := node.orig.Id() == i.selectedId

		if imgui.SelectableV(
			fmt.Sprintf("##instance_%d", node.orig.Id()),
			isSelected,
			imgui.SelectableFlagsNone,
			imgui.Vec2{Y: i.iconSize()},
		) {
			i.doSelect(node)
		}

		if isSelected && i.tmpDoScrollToInstance {
			imgui.SetScrollHereY(.5)
			i.tmpDoScrollToInstance = false
		}

		i.showContextMenu(node)

		imgui.SameLine()
		imgui.IndentV(i.textIndent())
		imgui.Text(node.name)
		imgui.UnindentV(i.textIndent())

		imgui.SameLine()
		imgui.IndentV(i.iconIndent())
		w.Image(imgui.TextureID(node.sprite.Texture()), i.iconSize(), i.iconSize()).
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
			Build()
		imgui.UnindentV(i.iconIndent())
	}
}
