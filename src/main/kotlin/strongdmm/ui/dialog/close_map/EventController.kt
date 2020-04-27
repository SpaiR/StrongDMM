package strongdmm.ui.dialog.close_map

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.ui.TriggerCloseMapDialogUi
import strongdmm.ui.dialog.close_map.model.CloseMapDialogStatus

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerCloseMapDialogUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen(event: Event<Dmm, CloseMapDialogStatus>) {
        state.isDoOpen = true
        state.eventToReply = event
    }
}
