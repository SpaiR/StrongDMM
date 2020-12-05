package strongdmm.ui.panel.opened_maps

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.ProviderActionService
import strongdmm.event.type.service.ProviderMapHolderService
import strongdmm.service.action.ActionBalanceStorage

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderMapHolderService.OpenedMaps::class.java, ::handleProviderOpenedMaps)
        EventBus.sign(ProviderActionService.ActionBalanceStorage::class.java, ::handleProviderActionBalanceStorage)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
    }

    private fun handleProviderOpenedMaps(event: Event<Set<Dmm>, Unit>) {
        state.providedOpenedMaps = event.body
    }

    private fun handleProviderActionBalanceStorage(event: Event<ActionBalanceStorage, Unit>) {
        state.providedActionBalanceStorage = event.body
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.selectedMap = event.body
    }

    private fun handleSelectedMapClosed() {
        state.selectedMap = null
    }
}
