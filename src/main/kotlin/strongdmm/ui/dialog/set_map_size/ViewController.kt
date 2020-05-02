package strongdmm.ui.dialog.set_map_size

import imgui.ImGui
import strongdmm.byond.dmm.MapSize
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerMapModifierService

class ViewController(
    private val state: State
) : EventHandler {
    fun doOk() {
        sendEvent(TriggerMapModifierService.ChangeMapSize(MapSize(state.newX.get(), state.newY.get(), state.newZ.get())))
        dispose()
    }

    fun doCancel() {
        dispose()
    }

    fun blockApplication() {
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }

    private fun dispose() {
        ImGui.closeCurrentPopup()
        sendEvent(Reaction.ApplicationBlockChanged(false))
    }
}
