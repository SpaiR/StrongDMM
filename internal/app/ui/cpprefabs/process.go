package cpprefabs

import (
	"fmt"
	"strings"

	"sdmm/internal/dmapi/dmvars"
	w "sdmm/internal/imguiext/widget"

	"github.com/SpaiR/imgui-go"
)

func (p *Prefabs) Process(int32) {
	if len(p.nodes) == 0 {
		imgui.TextDisabled("No prefab selected")
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
		imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{X: 0, Y: 0})
		imgui.Text(node.name)
		imgui.TextColored(imgui.Vec4{X: 0.6, Y: 0.6, Z: 0.6, W: 1}, describeVars(node.orig.Vars()))
		imgui.PopStyleVar()
		imgui.EndGroup()

		imgui.EndGroup()

		node.visHeight = imgui.ItemRectMax().Y - imgui.ItemRectMin().Y
	}
}

func describeVars(variables *dmvars.Variables) string {
	// "name" is already plainly visible and can be skipped.
	// "icon", "icon_state", "dir", and "color" do affect the sprite, but are
	// included in the list in case the effect is not obvious.
	b := strings.Builder{}
	for _, key := range variables.Iterate() {
		if key == "name" {
			continue
		}
		if b.Len() > 0 {
			b.WriteString("; ")
		}
		b.WriteString(key)
		b.WriteString(" = ")
		value, _ := variables.Value(key)
		b.WriteString(value)
	}
	return b.String()
}
