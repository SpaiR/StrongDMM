package strongdmm.ui.panel.variablespreview

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionTileItemService

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionTileItemService.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
    }

    private fun handleEnvironmentReset() {
        state.selectedTileItem = null
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        state.selectedTileItem = event.body
    }
}
