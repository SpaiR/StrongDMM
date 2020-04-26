package strongdmm.ui.close_map_dialog

import imgui.ImGui
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.ui.close_map_dialog.model.CloseMapDialogStatus

class ViewController(
    private val state: State
) : EventHandler {
    fun doDisposeWithSave() {
        dispose(CloseMapDialogStatus.CLOSE_WITH_SAVE)
    }

    fun doDisposeWithoutSave() {
        dispose(CloseMapDialogStatus.CLOSE)
    }

    fun doDisposeWithCancel() {
        dispose(CloseMapDialogStatus.CANCEL)
    }

    fun blockApplication() {
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }

    private fun dispose(status: CloseMapDialogStatus) {
        ImGui.closeCurrentPopup()
        sendEvent(Reaction.ApplicationBlockChanged(false))
        state.eventToReply?.reply(status)
        state.eventToReply = null
    }
}
