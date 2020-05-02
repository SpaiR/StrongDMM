package strongdmm.controller

import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
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

    private fun handleFilterLayersById(event: Event<LongArray, Unit>) {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowLayersByType(event: Event<String, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideLayersByType(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.contains(event.body) }.map { it.type })
            sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleFetchFilteredLayers(event: Event<Unit, Set<String>>) {
        event.reply(filteredTypes)
    }

    private fun handleEnvironmentReset() {
        filteredTypes.clear()
        sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
    }
}
