package strongdmm.ui.panel.opened_maps

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Provider.MapHolderControllerOpenedMaps::class.java, ::handleProviderMapHolderControllerOpenedMaps)
        consumeEvent(Provider.ActionControllerActionBalanceStorage::class.java, ::handleProviderActionControllerActionBalanceStorage)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
    }

    private fun handleProviderMapHolderControllerOpenedMaps(event: Event<Set<Dmm>, Unit>) {
        state.providedOpenedMaps = event.body
    }

    private fun handleProviderActionControllerActionBalanceStorage(event: Event<TObjectIntHashMap<Dmm>, Unit>) {
        state.providedActionBalanceStorage = event.body
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.selectedMap = event.body
    }

    private fun handleSelectedMapClosed() {
        state.selectedMap = null
    }
}
