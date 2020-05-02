package strongdmm.event.type.ui

import strongdmm.event.Event

abstract class TriggerInstanceLocatorPanelUi {
    class SearchByType(tileItemType: String) : Event<String, Unit>(tileItemType, null)
    class SearchById(tileItemId: Long) : Event<Long, Unit>(tileItemId, null)
}
