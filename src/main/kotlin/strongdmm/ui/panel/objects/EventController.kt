package strongdmm.ui.panel.objects

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerObjectPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(TriggerObjectPanelUi.Update::class.java, ::handleUpdate)
    }

    lateinit var viewController: ViewController

    private fun handleEnvironmentReset() {
        state.selectedTileItemType = ""
        state.tileItems = null
        state.selectedTileItemId = 0
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        if (event.body == null) {
            return
        }

        state.scrolledToItem = false
        state.selectedTileItemType = event.body.type
        state.tileItems = viewController.getTileItemsByTypeSorted(event.body.type)
        state.selectedTileItemId = state.tileItems!!.find { it.customVars == event.body.customVars }?.id ?: 0
    }

    private fun handleSelectedMapChanged() {
        viewController.updateTileItems()
    }

    private fun handleUpdate() {
        viewController.updateTileItems()
    }
}
