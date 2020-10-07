package strongdmm.ui.panel.opened_maps

import gnu.trove.map.hash.TObjectIntHashMap
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Provider.MapHolderServiceOpenedMaps::class.java, ::handleProviderMapHolderServiceOpenedMaps)
        EventBus.sign(Provider.ActionServiceActionBalanceStorage::class.java, ::handleProviderActionServiceActionBalanceStorage)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
    }

    private fun handleProviderMapHolderServiceOpenedMaps(event: Event<Set<Dmm>, Unit>) {
        state.providedOpenedMaps = event.body
    }

    private fun handleProviderActionServiceActionBalanceStorage(event: Event<TObjectIntHashMap<Dmm>, Unit>) {
        state.providedActionBalanceStorage = event.body
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.selectedMap = event.body
    }

    private fun handleSelectedMapClosed() {
        state.selectedMap = null
    }
}
