package strongdmm.controller

import strongdmm.event.*
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventLayersFilterController

class LayersFilterController : EventConsumer, EventSender {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(EventLayersFilterController.FilterLayersById::class.java, ::handleFilterLayersById)
        consumeEvent(EventLayersFilterController.ShowLayersByType::class.java, ::handleShowLayersByType)
        consumeEvent(EventLayersFilterController.HideLayersByType::class.java, ::handleHideLayersByType)
        consumeEvent(EventLayersFilterController.FetchFilteredLayers::class.java, ::handleFetchFilteredLayers)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterLayersById(event: Event<DmeItemIdArray, Unit>) {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowLayersByType(event: Event<DmeItemType, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideLayersByType(event: Event<DmeItemType, Unit>) {
        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.contains(event.body) }.map { it.type })
            sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleFetchFilteredLayers(event: Event<Unit, Set<DmeItemType>>) {
        event.reply(filteredTypes)
    }

    private fun handleEnvironmentReset() {
        filteredTypes.clear()
        sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
    }
}
