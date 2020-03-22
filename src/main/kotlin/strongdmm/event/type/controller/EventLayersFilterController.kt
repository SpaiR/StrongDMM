package strongdmm.event.type.controller

import strongdmm.event.DmeItemIdArray
import strongdmm.event.DmeItemType
import strongdmm.event.Event

abstract class EventLayersFilterController {
    class FilterLayersById(body: DmeItemIdArray) : Event<DmeItemIdArray, Unit>(body, null)
    class ShowLayersByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
    class HideLayersByType(body: DmeItemType) : Event<DmeItemType, Unit>(body, null)
    class FetchFilteredLayers(callback: ((Set<DmeItemType>) -> Unit)) : Event<Unit, Set<DmeItemType>>(Unit, callback)
}
