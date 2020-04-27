package strongdmm.controller

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerTileItemController

class TileItemController : EventSender, EventConsumer {
    private var selectedTileItem: TileItem? = null

    init {
        consumeEvent(TriggerTileItemController.ChangeSelectedTileItem::class.java, ::handleChangeSelectedTileItem)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleChangeSelectedTileItem(event: Event<TileItem, Unit>) {
        selectedTileItem = event.body
        sendEvent(Reaction.SelectedTileItemChanged(event.body))
    }

    private fun handleEnvironmentReset() {
        selectedTileItem = null
        sendEvent(Reaction.SelectedTileItemChanged(null))
    }
}
