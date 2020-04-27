package strongdmm.ui.coords_panel

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
    }

    private fun handleSelectedMapChanged() {
        state.isMapOpened = true
    }

    private fun handleSelectedMapClosed() {
        state.isMapOpened = false
    }

    private fun handleEnvironmentReset() {
        state.isMapOpened = false
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        state.xMapMousePos = event.body.x
        state.yMapMousePos = event.body.y
    }
}
