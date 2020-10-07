package strongdmm.ui.dialog.available_maps

import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerAvailableMapsDialogUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerAvailableMapsDialogUi.Open::class.java, ::handleOpen)
        EventBus.sign(Provider.MapHolderServiceAvailableMaps::class.java, ::handleProviderMapHolderServiceAvailableMaps)
    }

    private fun handleOpen() {
        state.isDoOpen = true
        state.isFirstOpen = true
        EventBus.post(Reaction.ApplicationBlockChanged(true))
    }

    private fun handleProviderMapHolderServiceAvailableMaps(event: Event<Set<MapPath>, Unit>) {
        state.providedAvailableMapPaths = event.body
    }
}
