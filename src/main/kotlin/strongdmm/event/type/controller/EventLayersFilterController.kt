package strongdmm.event.type.controller

import strongdmm.event.DmeItemIdArray
import strongdmm.event.DmeItemType
import strongdmm.event.Event

abstract class EventLayersFilterController {
    class FilterById(body: DmeItemIdArray) : Event<DmeItemIdArray, Unit>(body, null)
    class ShowByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
    class HideByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
    class Fetch(callback: ((Set<DmeItemType>) -> Unit)) : Event<Unit, Set<DmeItemType>>(Unit, callback)
}
