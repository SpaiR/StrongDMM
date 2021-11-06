package pmap

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/imguiext"
	w "sdmm/imguiext/widget"
)

type toolDesc struct {
	icon string
	help string
}

var (
	toolsOrder = []string{
		tools.TNAdd,
		tools.TNSelect,
	}

	toolsDesc = map[string]toolDesc{
		tools.TNAdd: {
			icon: imguiext.IconFaPlus,
			help: "Add (Key 1)\nClick - Place selected object topmost\nAlt+Click - Place selected object with replace",
		},
		tools.TNSelect: {
			icon: imguiext.IconFaEyeDropper,
			help: "Select (Hold Shift)\nClick - Select hovered object",
		},
	}
)

func (p *PaneMap) showToolsPanel() {
	p.updateShortcutsState()

	for idx, toolName := range toolsOrder {
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

		if idx != len(p.tools.Tools())-1 {
			imgui.SameLine()
		}
	}
}

// While holding a SHIFT key we will go into the selection mode.
func (p *PaneMap) processToolsSelectionMode() {
	isShiftPressed := imgui.IsKeyPressedV(int(glfw.KeyLeftShift), false) || imgui.IsKeyPressedV(int(glfw.KeyRightShift), false)
	isShiftReleased := imgui.IsKeyReleased(int(glfw.KeyLeftShift)) || imgui.IsKeyReleased(int(glfw.KeyRightShift))
	isSelectionMode := p.tools.IsSelected(tools.TNSelect)

	if isShiftPressed && !isSelectionMode {
		p.tmpLastSelectedTool = p.tools.Selected()
		p.tools.SetSelectedByName(tools.TNSelect)
	} else if isShiftReleased && p.tmpLastSelectedTool != nil {
		if isSelectionMode {
			p.tools.SetSelected(p.tmpLastSelectedTool)
		}
		p.tmpLastSelectedTool = nil
	}
}

func (p *PaneMap) selectAddTool() {
	p.tools.SetSelectedByName(tools.TNAdd)
}
