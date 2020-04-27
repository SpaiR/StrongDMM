package strongdmm.event.type.controller

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerTileItemController {
    class ChangeSelectedTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
}
