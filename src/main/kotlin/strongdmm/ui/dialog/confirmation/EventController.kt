package strongdmm.ui.dialog.confirmation

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.ui.TriggerConfirmationDialogUi
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogData
import strongdmm.ui.dialog.confirmation.model.ConfirmationDialogStatus

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerConfirmationDialogUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen(event: Event<ConfirmationDialogData, ConfirmationDialogStatus>) {
        state.isDoOpen = true
        state.data = event.body
        state.eventToReply = event
    }
}
