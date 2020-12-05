package strongdmm.ui.dialog.available_maps

import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.ProviderMapHolderService
import strongdmm.event.type.ui.TriggerAvailableMapsDialogUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerAvailableMapsDialogUi.Open::class.java, ::handleOpen)
        EventBus.sign(ProviderMapHolderService.AvailableMaps::class.java, ::handleProviderAvailableMaps)
    }

    private fun handleOpen() {
        state.isDoOpen = true
        state.isFirstOpen = true
        EventBus.post(Reaction.ApplicationBlockChanged(true))
    }

    private fun handleProviderAvailableMaps(event: Event<Set<MapPath>, Unit>) {
        state.providedAvailableMapPaths = event.body
    }
}
