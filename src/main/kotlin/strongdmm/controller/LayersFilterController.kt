package strongdmm.controller

import strongdmm.event.*

class LayersFilterController : EventConsumer, EventSender {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(Event.LayersFilterController.FilterById::class.java, ::handleFilterById)
        consumeEvent(Event.LayersFilterController.ShowByType::class.java, ::handleShowByType)
        consumeEvent(Event.LayersFilterController.HideByType::class.java, ::handleHideByType)
        consumeEvent(Event.LayersFilterController.Fetch::class.java, ::handleFetch)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
    }

    private fun handleFilterById(event: Event<DmeItemIdArray, Unit>) {
        sendEvent(Event.EnvironmentController.Fetch { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(Event.Global.RefreshFrame())
            sendEvent(Event.Global.RefreshLayersFilter(filteredTypes))
        })
    }

    private fun handleShowByType(event: Event<DmeItemType, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(Event.Global.RefreshFrame())
        sendEvent(Event.Global.RefreshLayersFilter(filteredTypes))
    }

    private fun handleHideByType(event: Event<DmeItemType, Unit>) {
        sendEvent(Event.EnvironmentController.Fetch { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.contains(event.body) }.map { it.type })
            sendEvent(Event.Global.RefreshFrame())
            sendEvent(Event.Global.RefreshLayersFilter(filteredTypes))
        })
    }

    private fun handleFetch(event: Event<Unit, Set<DmeItemType>>) {
        event.reply(filteredTypes)
    }

    private fun handleResetEnvironment() {
        filteredTypes.clear()
    }
}
