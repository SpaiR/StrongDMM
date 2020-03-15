package strongdmm.controller

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventTileItemController

class TileItemController : EventSender, EventConsumer {
    private var activeTileItem: TileItem? = null

    init {
        consumeEvent(EventTileItemController.ChangeActive::class.java, ::handleChangeActive)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleChangeActive(event: Event<TileItem, Unit>) {
        activeTileItem = event.body
        sendEvent(EventGlobal.ActiveTileItemChanged(event.body))
    }

    private fun handleEnvironmentReset() {
        activeTileItem = null
        sendEvent(EventGlobal.ActiveTileItemChanged(null))
    }
}
