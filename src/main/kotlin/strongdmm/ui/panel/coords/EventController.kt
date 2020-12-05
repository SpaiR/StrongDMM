package strongdmm.ui.panel.coords

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ReactionCanvasService
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.ReactionMapHolderService

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionCanvasService.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
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
