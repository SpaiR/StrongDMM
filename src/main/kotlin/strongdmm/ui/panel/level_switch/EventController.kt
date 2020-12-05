package strongdmm.ui.panel.level_switch

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.ReactionMapHolderService

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.selectedMap = event.body
    }

    private fun handleEnvironmentReset() {
        state.selectedMap = null
    }

    private fun handleSelectedMapClosed() {
        state.selectedMap = null
    }
}
