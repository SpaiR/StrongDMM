package strongdmm.ui.dialog.set_map_size

import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.event.type.ui.TriggerSetMapSizeDialogUi
import kotlin.math.max

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerSetMapSizeDialogUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen() {
        sendEvent(TriggerMapHolderController.FetchSelectedMap {
            state.newX.set(max(1, it.maxX))
            state.newY.set(max(1, it.maxY))
            state.newZ.set(max(1, it.maxZ))
            state.isDoOpen = true
        })
    }
}
