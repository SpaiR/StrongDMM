package strongdmm.event.service

import strongdmm.event.Event

abstract class TriggerLayersFilterService {
    class FilterLayersById(dmeItemIdArray: LongArray) : Event<LongArray, Unit>(dmeItemIdArray, null)
    class ShowLayersByTypeExact(dmeItemType: String) : Event<String, Unit>(dmeItemType, null)
    class HideLayersByTypeExact(dmeItemType: String) : Event<String, Unit>(dmeItemType, null)
    class FetchFilteredLayers(callback: ((Set<String>) -> Unit)) : Event<Unit, Set<String>>(Unit, callback)
}
