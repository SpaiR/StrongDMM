package strongdmm.ui.panel.coords

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
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
