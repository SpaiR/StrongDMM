package strongdmm.event.type

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class EventTileItemController {
    class ChangeActive(body: TileItem) : Event<TileItem, Unit>(body, null)
}
