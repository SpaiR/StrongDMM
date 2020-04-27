package strongdmm.ui.dialog.available_maps

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
        consumeEvent(Provider.MapHolderControllerAvailableMaps::class.java, ::handleProviderMapHolderControllerAvailableMaps)
    }

    private fun handleOpen() {
        state.isDoOpen = true
        state.isFirstOpen = true
        sendEvent(Reaction.ApplicationBlockChanged(true))
    }

    private fun handleProviderMapHolderControllerAvailableMaps(event: Event<Set<Pair<String, String>>, Unit>) {
        state.providedAvailableMaps = event.body
    }
}
