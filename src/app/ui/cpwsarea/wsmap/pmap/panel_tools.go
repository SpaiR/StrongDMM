package pmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"github.com/go-gl/glfw/v3.3/glfw"
	"log"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
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
		tools.TNGrab,
		tSeparator,
		tools.TNPick,
		tools.TNDelete,
	}

	toolsDesc = map[string]toolDesc{
		tools.TNAdd: {
			icon: icon.FaPlus,
			help: tools.TNAdd + " (1)\nClick - Place selected object\nAlt+Click - Place selected object with replace",
		},
		tools.TNFill: {
			icon: icon.FaBorderAll,
			help: tools.TNFill + " (2)\nClick&Drag - Fill the area with select object\nAlt+Click&Drag - Fill selected area with select object with replace",
		},
		tools.TNGrab: {
			icon: icon.FaBorderStyle,
			help: tools.TNGrab + " (3)\nClick&Drag - Select the area / Move selection with visible objects inside",
		},
		tools.TNPick: {
			icon: icon.FaEyeDropper,
			help: tools.TNPick + " (Hold S)\nClick - Pick hovered object",
		},
		tools.TNDelete: {
			icon: icon.FaEraser,
			help: tools.TNDelete + " (Hold D)\nClick - Delete hovered object\nAlt+Click - Delete tile",
		},
	}
)

func (p *PaneMap) showToolsPanel() {
	p.showTools()
	if p.dmm.MaxZ != 1 {
		imgui.SameLine()
		imgui.TextDisabled("|")
		p.showLevelButtons()
	}
}

func (p *PaneMap) showTools() {
	for idx, toolName := range toolsOrder {
		if idx > 0 || idx < len(toolsOrder)-1 {
			imgui.SameLine()
		}

		if toolName == tSeparator {
			imgui.TextDisabled("|")
			continue
		}

		tool := tools.Tools()[toolName]
		desc := toolsDesc[toolName]

		btn := w.Button(desc.icon, func() {
			tools.SetSelected(toolName)
		})
		if tools.Selected() == tool {
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

func (p *PaneMap) showLevelButtons() {
	imgui.BeginDisabledV(!p.hasPreviousLevel())
	imgui.SameLine()
	if imgui.Button(icon.FaArrowDown) {
		p.doPreviousLevel()
	}
	imguiext.SetItemHoveredTooltip(fmt.Sprintf("Previous z-level (%s+Down)", shortcut.KeyCmdName()))
	imgui.EndDisabled()

	imgui.BeginDisabledV(!p.hasNextLevel())
	imgui.SameLine()
	if imgui.Button(icon.FaArrowUp) {
		p.doNextLevel()
	}
	imguiext.SetItemHoveredTooltip(fmt.Sprintf("Next z-level (%s+Up)", shortcut.KeyCmdName()))
	imgui.EndDisabled()
}

func (p *PaneMap) processTempToolsMode() {
	if !p.tmpIsInTemporalToolMode {
		p.tmpLastSelectedToolName = tools.Selected().Name()
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
		p.tmpPrevSelectedToolName = tools.Selected().Name()
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

func (p *PaneMap) selectSelectTool() {
	tools.SetSelected(tools.TNGrab)
}

func (p *PaneMap) doPreviousLevel() {
	if p.hasPreviousLevel() {
		p.activeLevel--
		log.Println("[pmap] active level switched to previous:", p.activeLevel)
	}
}

func (p *PaneMap) doNextLevel() {
	if p.hasNextLevel() {
		p.activeLevel++
		log.Println("[pmap] active level switched to next:", p.activeLevel)
	}
}

func (p *PaneMap) hasPreviousLevel() bool {
	return p.activeLevel > 1
}

func (p *PaneMap) hasNextLevel() bool {
	return p.activeLevel < p.dmm.MaxZ
}
