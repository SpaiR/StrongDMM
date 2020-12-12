package strongdmm.event.service

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class TriggerTileItemService {
    class ChangeSelectedTileItem(body: TileItem) : Event<TileItem, Unit>(body, null)
    class ResetSelectedTileItem() : Event<Unit, Unit>(Unit, null)
}
