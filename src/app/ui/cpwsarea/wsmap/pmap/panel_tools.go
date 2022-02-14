package pmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
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
		}).Round(true)
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

	w.Button(icon.FaArrowDown, p.doPreviousLevel).
		Tooltip(fmt.Sprintf("Previous z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Down"))).
		Round(true).
		Build()

	imgui.EndDisabled()

	imgui.BeginDisabledV(!p.hasNextLevel())
	imgui.SameLine()

	w.Button(icon.FaArrowUp, p.doNextLevel).
		Tooltip(fmt.Sprintf("Next z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Up"))).
		Round(true).
		Build()

	imgui.EndDisabled()
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
