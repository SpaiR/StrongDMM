package cpwsarea

import (
	"fmt"
	"log"

	"github.com/SpaiR/imgui-go"
	"sdmm/app/command"
	"sdmm/app/ui/cpwsarea/workspace"
	"sdmm/app/ui/cpwsarea/workspace/wsempty"
	"sdmm/app/ui/cpwsarea/workspace/wsmap"
	"sdmm/dm/dmmap"
	"sdmm/imguiext"
)

type Action interface {
	wsempty.Action
	wsmap.Action

	AppUpdateTitle()
	AppSwitchCommandStack(id string)
	AppDisposeCommandStack(id string)
}

type WsArea struct {
	action Action

	activeWs workspace.Workspace

	workspaces []workspace.Workspace
}

func (w *WsArea) Init(action Action) {
	w.action = action
	w.addEmptyWorkspace()
}

func (w *WsArea) Process() {
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
}

func (w *WsArea) Free() {
	w.closeAllMaps()
	log.Println("[cpwsarea] workspace area free")
}

func (w *WsArea) OpenMap(dmm *dmmap.Dmm) {
	if ws, ok := w.mapWorkspace(dmm.Path); ok {
		ws.Select(true)
		return
	}

	idx := w.findEmptyWorkspaceIdx()
	ws := wsmap.New(w.action, dmm)
	if idx != -1 {
		w.closeWorkspaceByIdx(idx)
		w.addWorkspaceV(ws, idx)
	} else {
		w.addWorkspace(ws)
	}
}

func (w *WsArea) WorkspaceTitle() string {
	if w.activeWs != nil {
		return w.activeWs.Tooltip()
	}
	return ""
}

func (w *WsArea) ActiveWorkspace() workspace.Workspace {
	return w.activeWs
}

func (w *WsArea) closeAllMaps() {
	workspaces := make([]workspace.Workspace, len(w.workspaces))
	copy(workspaces, w.workspaces)
	for _, ws := range workspaces {
		if _, ok := ws.(*wsmap.WsMap); ok {
			w.closeWorkspace(ws)
		}
	}
	if w.findEmptyWorkspaceIdx() == -1 {
		w.addEmptyWorkspace()
	}
}

func (w *WsArea) mapWorkspace(path dmmap.DmmPath) (*wsmap.WsMap, bool) {
	for _, ws := range w.workspaces {
		if ws, ok := ws.(*wsmap.WsMap); ok {
			if ws.PaneMap.Dmm().Path == path {
				return ws, true
			}
		}
	}
	return nil, false
}

func (w *WsArea) addWorkspace(ws workspace.Workspace) {
	w.addWorkspaceV(ws, len(w.workspaces))
}

func (w *WsArea) addWorkspaceV(ws workspace.Workspace, idx int) {
	w.workspaces = append(w.workspaces[:idx], append([]workspace.Workspace{ws}, w.workspaces[idx:]...)...)
	log.Printf("[cpwsarea] workspace opened in index [%d]: %s", idx, ws.Name())
}

func (w *WsArea) closeWorkspace(ws workspace.Workspace) {
	if idx := w.findWorkspaceIdx(ws); idx != -1 {
		w.closeWorkspaceByIdx(idx)
	}
}

func (w *WsArea) closeWorkspaceByIdx(idx int) {
	ws := w.workspaces[idx]
	if ws.WantClose() {
		w.workspaces = append(w.workspaces[:idx], w.workspaces[idx+1:]...)
		ws.Dispose()
		log.Printf("[cpwsarea] workspace closed in idx [%d]: %s", idx, ws.Name())
		w.action.AppDisposeCommandStack(ws.CommandStackId())
	}
}

func (w *WsArea) addEmptyWorkspace() {
	w.addWorkspace(wsempty.New(w.action))
}

func (w *WsArea) findWorkspaceIdx(ws workspace.Workspace) int {
	for idx, lws := range w.workspaces {
		if lws == ws {
			return idx
		}
	}
	return -1
}

func (w *WsArea) findEmptyWorkspaceIdx() int {
	for idx, ws := range w.workspaces {
		if _, ok := ws.(*wsempty.WsEmpty); ok {
			return idx
		}
	}
	return -1
}

func (w *WsArea) switchActiveWorkspace(activeWs workspace.Workspace) {
	if w.activeWs != activeWs {
		w.activeWs = activeWs

		w.action.AppUpdateTitle()

		if activeWs == nil {
			w.action.AppSwitchCommandStack(command.NullSpaceStackId)
		} else {
			w.action.AppSwitchCommandStack(w.activeWs.CommandStackId())
		}
	}
}
