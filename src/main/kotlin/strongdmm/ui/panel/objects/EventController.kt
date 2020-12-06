package strongdmm.ui.panel.objects

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ProviderDmiService
import strongdmm.event.type.service.ReactionCanvasService
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.ReactionTileItemService
import strongdmm.event.type.ui.TriggerObjectPanelUi
import strongdmm.service.dmi.DmiCache

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderDmiService.DmiCache::class.java, ::handleProviderDmiCache)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionTileItemService.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(ReactionCanvasService.FrameRefreshed::class.java, ::handleFrameRefreshed)
        EventBus.sign(TriggerObjectPanelUi.Update::class.java, ::handleUpdate)
    }

    lateinit var viewController: ViewController

    private fun handleProviderDmiCache(event: Event<DmiCache, Unit>) {
        state.providedDmiCache = event.body
    }

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
