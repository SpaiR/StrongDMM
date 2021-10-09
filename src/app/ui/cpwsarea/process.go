package cpwsarea

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext"
)

func (w *WsArea) Process() {
	isCpInFocus := imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)

	// Window of the component doesn't have title bar, so we need to mock focus behaviour in that way.
	// Thus, if the window is unfocused that could be seen by tabs color.
	if !isCpInFocus {
		imgui.PushStyleColor(imgui.StyleColorTabActive, imgui.CurrentStyle().Color(imgui.StyleColorTabUnfocusedActive))
	}

	if imgui.BeginTabBarV("workspace_area", imgui.TabBarFlagsTabListPopupButton|imgui.TabBarFlagsAutoSelectNewTabs) {
		for idx, ws := range w.workspaces {
			open := true
			flags := imgui.TabItemFlagsNoTooltip

			if ws.IsDoSelect() {
				ws.Select(false)
				flags |= imgui.TabItemFlagsSetSelected
			}

			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{})
			if imgui.BeginTabItemV(ws.Name(), &open, flags) {
				imgui.PopStyleVar()

				w.switchActiveWorkspace(ws)

				if ws.HasTooltip() {
					imguiext.SetItemHoveredTooltip(ws.Tooltip())
				}

				if ws.Border() {
					imgui.PushStyleVarFloat(imgui.StyleVarChildBorderSize, 1)
				}

				imgui.BeginChildV(fmt.Sprint("workspace_", ws.Name(), idx), imgui.Vec2{}, ws.Border(), imgui.WindowFlagsNone)
				if ws.Border() {
					imgui.PopStyleVar()
				}
				ws.Process()
				imgui.EndChild()

				imgui.EndTabItem()
			} else {
				imgui.PopStyleVar()
				if ws.HasTooltip() {
					imguiext.SetItemHoveredTooltip(ws.Tooltip())
				}
			}

			if !open {
				w.closeWorkspaceByIdx(idx)
			}
		}

		if imgui.TabItemButton(imguiext.IconFaPlus) {
			w.addEmptyWorkspace()
		}

		imgui.EndTabBar()
	}

	if !isCpInFocus {
		imgui.PopStyleColor()
	}
}
