package pmap

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

type toolDesc struct {
	icon string
	help string
}

const tSeparator = "toolsSeparator"

var (
	toolsOrder = []string{
		tools.TNAdd,
		tSeparator,
		tools.TNSelect,
		tools.TNDelete,
	}

	toolsDesc = map[string]toolDesc{
		tools.TNAdd: {
			icon: imguiext.IconFaPlus,
			help: "Add (Key 1)\nClick - Place selected object topmost\nAlt+Click - Place selected object with replace",
		},
		tools.TNSelect: {
			icon: imguiext.IconFaEyeDropper,
			help: "Select (Hold S Key)\nClick - Select hovered object",
		},
		tools.TNDelete: {
			icon: imguiext.IconFaEraser,
			help: "Delete (Hold D Key)\nClick - Delete hovered object\nAlt+Click - Delete tile",
		},
	}
)

func (p *PaneMap) showToolsPanel() {
	p.updateShortcutsState()

	for idx, toolName := range toolsOrder {
		if idx > 0 || idx < len(toolsOrder)-1 {
			imgui.SameLine()
		}

		if toolName == tSeparator {
			imgui.TextDisabled("|")
			continue
		}

		tool := p.tools.Tools()[toolName]
		desc := toolsDesc[toolName]

		btn := w.Button(desc.icon, func() {
			p.tools.SetSelected(tool)
		})
		if p.tools.Selected() == tool {
			if tool.AltBehaviour() {
				btn.Style(imguiext.StyleButtonRed{})
			} else {
				btn.Style(imguiext.StyleButtonGreen{})
			}
		}
		btn.Build()

		imguiext.SetItemHoveredTooltip(desc.help)
	}
}

func (p *PaneMap) processToolsTemporalMode(key, altKey int, modeName string) {
	isKeyPressed := imgui.IsKeyPressedV(key, false) || imgui.IsKeyPressedV(altKey, false)
	isKeyReleased := imgui.IsKeyReleased(key) || imgui.IsKeyReleased(altKey)
	isSelected := p.tools.IsSelected(modeName)

	if isKeyPressed && !isSelected {
		p.tmpLastSelectedTool = p.tools.Selected()
		p.tools.SetSelectedByName(modeName)
	} else if isKeyReleased && p.tmpLastSelectedTool != nil {
		if isSelected {
			p.tools.SetSelected(p.tmpLastSelectedTool)
		}
		p.tmpLastSelectedTool = nil
	}
}

func (p *PaneMap) selectAddTool() {
	p.tools.SetSelectedByName(tools.TNAdd)
}