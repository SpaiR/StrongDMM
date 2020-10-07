package strongdmm.ui.dialog.set_map_size

import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerMapHolderService
import strongdmm.event.type.ui.TriggerSetMapSizeDialogUi
import kotlin.math.max

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerSetMapSizeDialogUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen() {
        EventBus.post(TriggerMapHolderService.FetchSelectedMap {
            state.newX.set(max(1, it.maxX))
            state.newY.set(max(1, it.maxY))
            state.newZ.set(max(1, it.maxZ))
            state.isDoOpen = true
        })
    }
}
