package strongdmm.event.type.service

import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event

abstract class ReactionTileItemService {
    class SelectedTileItemChanged(body: TileItem?) : Event<TileItem?, Unit>(body, null)
}
