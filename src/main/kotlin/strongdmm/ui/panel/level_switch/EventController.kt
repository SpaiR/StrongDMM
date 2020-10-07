package strongdmm.ui.panel.level_switch

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
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
