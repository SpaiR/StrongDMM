package pmap

import (
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"sdmm/app/ui/cpwsarea/workspace/wsmap/pmap/tools"
	"sdmm/imguiext"
	"sdmm/imguiext/icon"
	"sdmm/imguiext/style"
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
		tools.TNFill,
		tSeparator,
		tools.TNPick,
		tools.TNDelete,
	}

	toolsDesc = map[string]toolDesc{
		tools.TNAdd: {
			icon: icon.FaPlus,
			help: "Add (1)\nClick - Place selected object\nAlt+Click - Place selected object with replace",
		},
		tools.TNFill: {
			icon: icon.FaBorderAll,
			help: "Fill (2)\nClick&Drag - Fill the area with select object\nAlt+Click&Drag - Fill selected area with select object with replace",
		},
		tools.TNPick: {
			icon: icon.FaEyeDropper,
			help: "Pick (Hold S)\nClick - Pick hovered object",
		},
		tools.TNDelete: {
			icon: icon.FaEraser,
			help: "Delete (Hold D)\nClick - Delete hovered object\nAlt+Click - Delete tile",
		},
	}
)

func (p *PaneMap) showToolsPanel() {
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
			tools.SetSelected(toolName)
		})
		if p.tools.Selected() == tool {
			if tool.AltBehaviour() {
				btn.Style(style.ButtonGold{}).TextColor(style.ColorBlack)
			} else {
				btn.Style(style.ButtonGreen{})
			}
		}
		btn.Build()

		imguiext.SetItemHoveredTooltip(desc.help)
	}
}

func (p *PaneMap) processTempToolsMode() {
	if !p.tmpIsInTemporalToolMode {
		p.tmpLastSelectedToolName = p.tools.Selected().Name()
	}

	var inMode bool
	inMode = inMode || p.processTempToolMode(int(glfw.KeyS), -1, tools.TNPick)
	inMode = inMode || p.processTempToolMode(int(glfw.KeyD), -1, tools.TNDelete)

	if p.tmpIsInTemporalToolMode && !inMode {
		tools.SetSelected(p.tmpLastSelectedToolName)
		p.tmpLastSelectedToolName = ""
		p.tmpIsInTemporalToolMode = false
	}
}

func (p *PaneMap) processTempToolMode(key, altKey int, modeName string) bool {
	// Ignore presses when Dear ImGui inputs are in charge or actual shortcuts are invisible.
	if !p.shortcuts.Visible() {
		return false
	}

	isKeyPressed := imgui.IsKeyPressedV(key, false) || imgui.IsKeyPressedV(altKey, false)
	isKeyReleased := imgui.IsKeyReleased(key) || imgui.IsKeyReleased(altKey)
	isKeyDown := imgui.IsKeyDown(key) || imgui.IsKeyDown(altKey)
	isSelected := tools.IsSelected(modeName)

	if isKeyPressed && !isSelected {
		p.tmpPrevSelectedToolName = p.tools.Selected().Name()
		p.tmpIsInTemporalToolMode = true
		tools.SetSelected(modeName)
	} else if isKeyReleased && len(p.tmpPrevSelectedToolName) != 0 {
		if isSelected {
			tools.SetSelected(p.tmpPrevSelectedToolName)
		}
		p.tmpPrevSelectedToolName = ""
	}

	return isKeyDown
}

func (p *PaneMap) selectAddTool() {
	tools.SetSelected(tools.TNAdd)
}

func (p *PaneMap) selectFillTool() {
	tools.SetSelected(tools.TNFill)
}
