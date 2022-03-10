package pmap

import (
	"log"
	"sdmm/app/ui/cpwsarea/wsmap/tools"
	"sdmm/imguiext/icon" //nolint
	"sdmm/imguiext/style"
	w "sdmm/imguiext/widget"
)

type toolDesc struct {
	btnIcon string
	tooltip w.Layout
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
			btnIcon: icon.Add,
			tooltip: w.Layout{
				w.AlignTextToFramePadding(),
				w.Text(tools.TNAdd),
				w.SameLine(),
				w.TextFrame("1"),
				w.Separator(),
				w.Text("Click - Place selected object\nAlt+Click - Place selected object with replace"),
			},
		},
		tools.TNFill: {
			btnIcon: icon.BorderAll,
			tooltip: w.Layout{
				w.AlignTextToFramePadding(),
				w.Text(tools.TNFill),
				w.SameLine(),
				w.TextFrame("2"),
				w.Separator(),
				w.Text("Click&Drag - Fill the area with select object\nAlt+Click&Drag - Fill selected area with select object with replace"),
			},
		},
		tools.TNGrab: {
			btnIcon: icon.BorderStyle,
			tooltip: w.Layout{
				w.AlignTextToFramePadding(),
				w.Text(tools.TNGrab),
				w.SameLine(),
				w.TextFrame("3"),
				w.Separator(),
				w.Text("Click&Drag - Select the area / Move selection with visible objects inside"),
			},
		},
		tools.TNPick: {
			btnIcon: icon.EyeDropper,
			tooltip: w.Layout{
				w.AlignTextToFramePadding(),
				w.Text(tools.TNPick),
				w.SameLine(),
				w.TextFrame("Hold S"),
				w.Separator(),
				w.Text("Click - Pick hovered object"),
			},
		},
		tools.TNDelete: {
			btnIcon: icon.Eraser,
			tooltip: w.Layout{
				w.AlignTextToFramePadding(),
				w.Text(tools.TNDelete),
				w.SameLine(),
				w.TextFrame("Hold D"),
				w.Separator(),
				w.Text("Click - Delete hovered object\nAlt+Click - Delete tile"),
			},
		},
	}
)

func (p *PaneMap) showToolsPanel() {
	p.layoutTools().Build()
}

func (p *PaneMap) layoutTools() (layout w.Layout) {
	for idx, toolName := range toolsOrder {
		var toolName = toolName // Closure (hello, js)

		if idx > 0 || idx < len(toolsOrder)-1 {
			layout = append(layout, w.SameLine())
		}

		if toolName == tSeparator {
			layout = append(layout, w.TextDisabled("|"))
			continue
		}

		tool := tools.Tools()[toolName]
		desc := toolsDesc[toolName]

		btn := w.Button(desc.btnIcon, func() {
			tools.SetSelected(toolName)
		}).Round(true)

		if tools.Selected() == tool {
			if tool.AltBehaviour() {
				btn.Style(style.ButtonGold{}).TextColor(style.ColorBlack)
			} else {
				btn.Style(style.ButtonGreen{})
			}
		}

		layout = append(layout, btn, w.Tooltip(desc.tooltip))
	}
	return layout
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
