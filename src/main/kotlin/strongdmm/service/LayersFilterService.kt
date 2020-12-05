package strongdmm.service

import strongdmm.application.Service
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.ReactionLayersFilterService
import strongdmm.event.type.service.TriggerEnvironmentService
import strongdmm.event.type.service.TriggerLayersFilterService

class LayersFilterService : Service {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        EventBus.sign(TriggerLayersFilterService.FilterLayersById::class.java, ::handleFilterLayersById)
        EventBus.sign(TriggerLayersFilterService.ShowLayersByTypeExact::class.java, ::handleShowLayersByTypeExact)
        EventBus.sign(TriggerLayersFilterService.HideLayersByTypeExact::class.java, ::handleHideLayersByTypeExact)
        EventBus.sign(TriggerLayersFilterService.FetchFilteredLayers::class.java, ::handleFetchFilteredLayers)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterLayersById(event: Event<LongArray, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            EventBus.post(ReactionLayersFilterService.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowLayersByTypeExact(event: Event<String, Unit>) {
        filteredTypes.removeIf { it.startsWith(event.body) }
        EventBus.post(ReactionLayersFilterService.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideLayersByTypeExact(event: Event<String, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.startsWith(event.body) }.map { it.type })
            EventBus.post(ReactionLayersFilterService.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleFetchFilteredLayers(event: Event<Unit, Set<String>>) {
        event.reply(filteredTypes)
    }

    private fun handleEnvironmentReset() {
        filteredTypes.clear()
        EventBus.post(ReactionLayersFilterService.LayersFilterRefreshed(filteredTypes))
    }
}
