package cpwsarea

import (
	"fmt"

	"github.com/SpaiR/imgui-go"
	"sdmm/imguiext/icon"
)

func (w *WsArea) Process() {
	if len(w.workspaces) == 0 {
		w.switchActiveWorkspace(nil)
	}

	cpFocused := imgui.IsWindowFocusedV(imgui.FocusedFlagsRootAndChildWindows)

	// Window of the component doesn't have a title bar, so we need to mock a focus behaviour.
	// Thus, if the window is unfocused that could be seen by tabs color.
	inactiveStyle := !(w.focused || cpFocused)
	if inactiveStyle {
		imgui.PushStyleColor(imgui.StyleColorTabActive, imgui.CurrentStyle().Color(imgui.StyleColorTabUnfocusedActive))
	}

	if imgui.BeginTabBarV("workspace_area", imgui.TabBarFlagsTabListPopupButton|imgui.TabBarFlagsAutoSelectNewTabs) {
		for idx, ws := range w.workspaces { // Iterate through all workspaces.
			ws.SetIdx(idx)
			ws.PreProcess()

			open := true
			flags := imgui.TabItemFlagsNoTooltip // Not showing any tooltips by default.

			// If the workspace is requested to be selected - enforce the tab selection.
			if ws.IsDoSelect() {
				ws.Select(false)
				flags |= imgui.TabItemFlagsSetSelected
			}

			imgui.PushStyleVarVec2(imgui.StyleVarItemSpacing, imgui.Vec2{}) // Remove a default tab content padding.
			if imgui.BeginTabItemV(ws.Name(), &open, flags) {
				imgui.PopStyleVar()

				// Track a current active workspace by the currently selected tab.
				w.switchActiveWorkspace(ws)

				// Some workspaces require a border to render their content properly (visible better).
				if ws.Border() {
					imgui.PushStyleVarFloat(imgui.StyleVarChildBorderSize, 1)
				}
				imgui.BeginChildV(fmt.Sprint("workspace_", ws.Name(), idx), imgui.Vec2{}, ws.Border(), imgui.WindowFlagsNone)
				if ws.Border() {
					imgui.PopStyleVar()
				}

				// The actual processing of the workspace with showing of its content.
				ws.Process()

				// In some cases a content of the tba may consist of the multiple windows.
				// So we won't be able to track a "focus" state of the tab.
				// To avoid that problem we determine the tab activeness with the focus of the workspace
				// and the focus of the tab itself.
				w.focused = cpFocused || ws.Focused()

				imgui.EndChild()
				imgui.EndTabItem()
			} else {
				imgui.PopStyleVar()
			}

			// We store currently opened workspaces programmatically to be able to track their order inside of tabs.
			// When the tab is closed - close the workspace itself (basically means disposing).
			if !open {
				w.closeWorkspaceByIdx(idx)
			}
		}

		// Add an empty workspace when the "plus" button is clicked.
		if imgui.TabItemButton(icon.FaPlus) {
			w.addEmptyWorkspace()
		}

		imgui.EndTabBar()
	}

	if inactiveStyle {
		imgui.PopStyleColor()
	}
}
