package strongdmm.service

import strongdmm.Service
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerLayersFilterService

class LayersFilterService : Service, EventHandler {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(TriggerLayersFilterService.FilterLayersById::class.java, ::handleFilterLayersById)
        consumeEvent(TriggerLayersFilterService.ShowLayersByType::class.java, ::handleShowLayersByType)
        consumeEvent(TriggerLayersFilterService.HideLayersByType::class.java, ::handleHideLayersByType)
        consumeEvent(TriggerLayersFilterService.FetchFilteredLayers::class.java, ::handleFetchFilteredLayers)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterLayersById(event: Event<LongArray, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowLayersByType(event: Event<String, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(Reaction.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideLayersByType(event: Event<String, Unit>) {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
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
