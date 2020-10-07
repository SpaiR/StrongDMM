package strongdmm.ui.panel.objects

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerObjectPanelUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(Reaction.FrameRefreshed::class.java, ::handleFrameRefreshed)
        EventBus.sign(TriggerObjectPanelUi.Update::class.java, ::handleUpdate)
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

    private fun handleFrameRefreshed() {
        viewController.updateTileItems()
    }

    private fun handleUpdate() {
        viewController.updateTileItems()
    }
}
