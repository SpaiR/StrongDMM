package strongdmm.ui.panel.variables_preview

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
    }

    private fun handleEnvironmentReset() {
        state.selectedTileItem = null
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        state.selectedTileItem = event.body
    }
}
