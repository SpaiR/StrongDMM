package strongdmm.ui.panel.level_switch

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
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
