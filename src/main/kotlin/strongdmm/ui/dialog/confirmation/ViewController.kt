package strongdmm.ui.dialog.confirmation

import imgui.ImGui
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

class ViewController(
    private val state: State
) : EventHandler {
    fun doYes() {
        val event = state.eventToReply
        dispose()
        event?.reply(ConfirmationDialogStatus.YES)
    }

    fun doNo() {
        val event = state.eventToReply
        dispose()
        event?.reply(ConfirmationDialogStatus.NO)
    }

    fun doCancel() {
        val event = state.eventToReply
        dispose()
        event?.reply(ConfirmationDialogStatus.CANCEL)
    }

    private fun dispose() {
        ImGui.closeCurrentPopup()
        sendEvent(Reaction.ApplicationBlockChanged(false))
        state.eventToReply = null
    }

    fun blockApplication() {
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }
}
