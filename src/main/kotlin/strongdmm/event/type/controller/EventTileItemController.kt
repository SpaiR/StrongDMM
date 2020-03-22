package strongdmm.event.type.controller

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class EventTileItemController {
    class ChangeActiveTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
}
