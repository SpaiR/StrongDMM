package strongdmm.ui.panel.openedmaps

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ProviderActionService
import strongdmm.event.service.ProviderMapHolderService
import strongdmm.event.service.ReactionMapHolderService
import strongdmm.service.action.ActionBalanceStorage

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderMapHolderService.OpenedMaps::class.java, ::handleProviderOpenedMaps)
        EventBus.sign(ProviderActionService.ActionBalanceStorage::class.java, ::handleProviderActionBalanceStorage)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
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
