package strongdmm.event.type.ui

import strongdmm.event.Event
import strongdmm.event.TileItemId
import strongdmm.event.TileItemType

abstract class TriggerInstanceLocatorPanelUi {
    class SearchByType(body: TileItemType) : Event<TileItemType, Unit>(body, null)
    class SearchById(body: TileItemId) : Event<TileItemId, Unit>(body, null)
}
