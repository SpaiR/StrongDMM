package pmap

import (
	"fmt"
	"github.com/SpaiR/imgui-go"
	"log"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/app/ui/shortcut"
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
			icon: icon.Add,
			help: tools.TNAdd + " (1)\nClick - Place selected object\nAlt+Click - Place selected object with replace",
		},
		tools.TNFill: {
			icon: icon.BorderAll,
			help: tools.TNFill + " (2)\nClick&Drag - Fill the area with select object\nAlt+Click&Drag - Fill selected area with select object with replace",
		},
		tools.TNGrab: {
			icon: icon.BorderStyle,
			help: tools.TNGrab + " (3)\nClick&Drag - Select the area / Move selection with visible objects inside",
		},
		tools.TNPick: {
			icon: icon.EyeDropper,
			help: tools.TNPick + " (Hold S)\nClick - Pick hovered object",
		},
		tools.TNDelete: {
			icon: icon.Eraser,
			help: tools.TNDelete + " (Hold D)\nClick - Delete hovered object\nAlt+Click - Delete tile",
		},
	}
)

func (p *PaneMap) showToolsPanel() {
	w.Layout{
		p.layoutTools(),
	}.Build()
	if p.dmm.MaxZ != 1 {
		imgui.SameLine()
		w.Layout{
			p.layoutLevels(),
		}.BuildV(w.AlignRight)
	}
}

func (p *PaneMap) layoutTools() (layout w.Layout) {
	for idx, toolName := range toolsOrder {
		if idx > 0 || idx < len(toolsOrder)-1 {
			layout = append(layout, w.SameLine())
		}

		if toolName == tSeparator {
			layout = append(layout, w.TextDisabled("|"))
			continue
		}

		tool := tools.Tools()[toolName]
		desc := toolsDesc[toolName]

		btn := w.Button(desc.icon, func() {
			tools.SetSelected(toolName)
		}).Round(true).Tooltip(desc.help)

		if tools.Selected() == tool {
			if tool.AltBehaviour() {
				btn.Style(style.ButtonGold{}).TextColor(style.ColorBlack)
			} else {
				btn.Style(style.ButtonGreen{})
			}
		}

		layout = append(layout, btn)
	}
	return layout
}

func (p *PaneMap) layoutLevels() (layout w.Layout) {
	return w.Layout{
		w.Disabled(!p.hasPreviousLevel(), w.Layout{
			w.Button(icon.ArrowDownward, p.doPreviousLevel).
				Tooltip(fmt.Sprintf("Previous z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Down"))).
				Round(true),
		}),
		w.SameLine(),
		w.Disabled(!p.hasNextLevel(), w.Layout{
			w.Button(icon.ArrowUpward, p.doNextLevel).
				Tooltip(fmt.Sprintf("Next z-level (%s)", shortcut.Combine(shortcut.KeyModName(), "Up"))).
				Round(true),
		}),
	}
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
