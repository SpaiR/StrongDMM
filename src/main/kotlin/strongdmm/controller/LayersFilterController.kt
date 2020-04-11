package strongdmm.controller

import strongdmm.event.*
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.controller.TriggerLayersFilterController

class LayersFilterController : EventConsumer, EventSender {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(TriggerLayersFilterController.FilterLayersById::class.java, ::handleFilterLayersById)
        consumeEvent(TriggerLayersFilterController.ShowLayersByType::class.java, ::handleShowLayersByType)
        consumeEvent(TriggerLayersFilterController.HideLayersByType::class.java, ::handleHideLayersByType)
        consumeEvent(TriggerLayersFilterController.FetchFilteredLayers::class.java, ::handleFetchFilteredLayers)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterLayersById(event: Event<DmeItemIdArray, Unit>) {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowLayersByType(event: Event<DmeItemType, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideLayersByType(event: Event<DmeItemType, Unit>) {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.contains(event.body) }.map { it.type })
            sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleFetchFilteredLayers(event: Event<Unit, Set<DmeItemType>>) {
        event.reply(filteredTypes)
    }

    private fun handleEnvironmentReset() {
        filteredTypes.clear()
        sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
    }
}
