package strongdmm.service

import strongdmm.application.Service
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionTileItemService
import strongdmm.event.service.TriggerTileItemService

class TileItemService : Service {
    private var selectedTileItem: TileItem? = null

    init {
        EventBus.sign(TriggerTileItemService.ChangeSelectedTileItem::class.java, ::handleChangeSelectedTileItem)
        EventBus.sign(TriggerTileItemService.ResetSelectedTileItem::class.java, ::handleResetSelectedTileItem)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleChangeSelectedTileItem(event: Event<TileItem, Unit>) {
        selectedTileItem = event.body
        EventBus.post(ReactionTileItemService.SelectedTileItemChanged(event.body))
    }

    private fun handleResetSelectedTileItem() {
        selectedTileItem = null
        EventBus.post(ReactionTileItemService.SelectedTileItemChanged(null))
    }

    private fun handleEnvironmentReset() {
        selectedTileItem = null
        EventBus.post(ReactionTileItemService.SelectedTileItemChanged(null))
    }
}
