package cpwsarea

import (
	"github.com/SpaiR/imgui-go"
	"sdmm/app/ui/cpwsarea/workspace"
)

func (w *WsArea) Process(dockId int) {
	if len(w.workspaces) == 0 {
		w.switchActiveWorkspace(nil)
	}

	for idx, ws := range w.workspaces {
		ws.PreProcess()

		// When the window of the workspace is closed we need to dispose its content as well.
		if !w.showWorkspaceWindow(dockId, ws) {
			w.closeWorkspaceByIdx(idx)
		}

		ws.PostProcess()
	}
}

func (w *WsArea) showWorkspaceWindow(dockId int, ws *workspace.Workspace) bool {
	open := true
	id := ws.Name() + "###" + ws.Id()

	dockCondition := imgui.ConditionOnce
	if w.app.IsLayoutReset() {
		dockCondition = imgui.ConditionAlways
	}

	imgui.SetNextWindowDockIDV(dockId, dockCondition)

	flags := imgui.WindowFlagsNoSavedSettings | ws.Content().Ini().WindowFlags

	if ws.Content().Ini().NoPadding {
		imgui.PushStyleVarVec2(imgui.StyleVarWindowPadding, imgui.Vec2{})
	}

	if imgui.BeginV(id, &open, flags) {
		if ws.Content().Ini().NoPadding {
			imgui.PopStyleVar()
		}
		ws.Process()
	} else if ws.Content().Ini().NoPadding {
		imgui.PopStyleVar()
	}

	if ws.Focused() {
		w.switchActiveWorkspace(ws)
	} else if ws.TriggerFocus() {
		ws.SetTriggerFocus(false)
		imgui.SetWindowFocus()
	}

	imgui.End()

	return open
}
