package component

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"

	"github.com/SpaiR/strongdmm/internal/app/ui/component/workspace"
	"github.com/SpaiR/strongdmm/pkg/dm/dmmap"
	"github.com/SpaiR/strongdmm/pkg/imguiext"
)

type WorkspaceAreaAction interface {
	workspace.EmptyAction
	workspace.MapAction
}

type WorkspaceArea struct {
	action WorkspaceAreaAction

	workspaces []workspace.Workspace
}

func (w *WorkspaceArea) Init(action WorkspaceAreaAction) {
	w.action = action
	w.addEmptyWorkspace()
}

func (w *WorkspaceArea) Process() {
	if imgui.BeginTabBarV("workspace_area", imgui.TabBarFlagsTabListPopupButton|imgui.TabBarFlagsAutoSelectNewTabs) {
		for idx, ws := range w.workspaces {
			open := true
			flags := imgui.TabItemFlagsNoTooltip

			if ws.IsSelect() {
				ws.Select(false)
				flags |= imgui.TabItemFlagsSetSelected
			}

			if imgui.BeginTabItemV(ws.Name(), &open, flags) {
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
				if ws.HasTooltip() {
					imguiext.SetItemHoveredTooltip(ws.Tooltip())
				}
			}

			if !open {
				w.closeWorkspaceByIdx(idx)
			}
		}

		if imgui.TabItemButton("+") {
			w.addEmptyWorkspace()
		}

		imgui.EndTabBar()
	}
}

func (w *WorkspaceArea) Free() {
	w.closeAllMaps()
	log.Println("[component] workspace area free")
}

func (w *WorkspaceArea) OpenMap(dmm *dmmap.Dmm) {
	if ws, ok := w.mapWorkspace(dmm.Path); ok {
		ws.Select(true)
		return
	}

	idx := w.findEmptyWorkspaceIdx()
	ws := workspace.NewMap(w.action, dmm)
	if idx != -1 {
		w.closeWorkspaceByIdx(idx)
		w.addWorkspaceV(ws, idx)
	} else {
		w.addWorkspace(ws)
	}
}

func (w *WorkspaceArea) closeAllMaps() {
	workspaces := make([]workspace.Workspace, len(w.workspaces))
	copy(workspaces, w.workspaces)
	for _, ws := range workspaces {
		if _, ok := ws.(*workspace.Map); ok {
			w.closeWorkspace(ws)
		}
	}
}

func (w *WorkspaceArea) mapWorkspace(path dmmap.DmmPath) (*workspace.Map, bool) {
	for _, ws := range w.workspaces {
		if ws, ok := ws.(*workspace.Map); ok {
			if ws.Dmm.Path == path {
				return ws, true
			}
		}
	}
	return nil, false
}

func (w *WorkspaceArea) addWorkspace(ws workspace.Workspace) {
	w.addWorkspaceV(ws, len(w.workspaces))
}

func (w *WorkspaceArea) addWorkspaceV(ws workspace.Workspace, idx int) {
	w.workspaces = append(w.workspaces[:idx], append([]workspace.Workspace{ws}, w.workspaces[idx:]...)...)
	log.Printf("[component] workspace opened in index [%d]: %s", idx, ws.Name())
}

func (w *WorkspaceArea) closeWorkspace(ws workspace.Workspace) {
	if idx := w.findWorkspaceIdx(ws); idx != -1 {
		w.closeWorkspaceByIdx(idx)
	}
}

func (w *WorkspaceArea) closeWorkspaceByIdx(idx int) {
	ws := w.workspaces[idx]
	if ws.WantClose() {
		w.workspaces = append(w.workspaces[:idx], w.workspaces[idx+1:]...)
		ws.Dispose()
		log.Printf("[component] workspace closed in idx [%d]: %s", idx, ws.Name())
	}
}

func (w *WorkspaceArea) addEmptyWorkspace() {
	w.addWorkspace(workspace.NewEmpty(w.action))
}

func (w *WorkspaceArea) findWorkspaceIdx(ws workspace.Workspace) int {
	for idx, lws := range w.workspaces {
		if lws == ws {
			return idx
		}
	}
	return -1
}

func (w *WorkspaceArea) findEmptyWorkspaceIdx() int {
	for idx, ws := range w.workspaces {
		if _, ok := ws.(*workspace.Empty); ok {
			return idx
		}
	}
	return -1
}
