package strongdmm.ui.dialog.available_maps

import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerAvailableMapsDialogUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerAvailableMapsDialogUi.Open::class.java, ::handleOpen)
        consumeEvent(Provider.MapHolderServiceAvailableMaps::class.java, ::handleProviderMapHolderServiceAvailableMaps)
    }

    private fun handleOpen() {
        state.isDoOpen = true
        state.isFirstOpen = true
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }

    private fun handleProviderMapHolderServiceAvailableMaps(event: Event<Set<MapPath>, Unit>) {
        state.providedAvailableMapPaths = event.body
    }
}
