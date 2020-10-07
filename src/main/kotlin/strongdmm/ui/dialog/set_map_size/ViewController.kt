package strongdmm.ui.dialog.set_map_size

import imgui.ImGui
import strongdmm.byond.dmm.MapSize
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerMapModifierService

class ViewController(
    private val state: State
) {
    fun doOk() {
        EventBus.post(TriggerMapModifierService.ChangeMapSize(MapSize(state.newX.get(), state.newY.get(), state.newZ.get())))
        dispose()
    }

    fun doCancel() {
        dispose()
    }

    fun blockApplication() {
        EventBus.post(Reaction.ApplicationBlockChanged(true))
    }

    private fun dispose() {
        ImGui.closeCurrentPopup()
        EventBus.post(Reaction.ApplicationBlockChanged(false))
    }
}
