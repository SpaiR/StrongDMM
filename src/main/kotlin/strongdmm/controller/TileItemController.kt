package strongdmm.controller

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerTileItemController

class TileItemController : EventSender, EventConsumer {
    private var activeTileItem: TileItem? = null

    init {
        consumeEvent(TriggerTileItemController.ChangeActiveTileItem::class.java, ::handleChangeActive)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleChangeActive(event: Event<TileItem, Unit>) {
        activeTileItem = event.body
        sendEvent(Reaction.ActiveTileItemChanged(event.body))
    }

    private fun handleEnvironmentReset() {
        activeTileItem = null
        sendEvent(Reaction.ActiveTileItemChanged(null))
    }
}
