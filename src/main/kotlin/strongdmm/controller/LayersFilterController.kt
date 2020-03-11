package strongdmm.controller

import strongdmm.event.*
import strongdmm.event.type.EventFrameController
import strongdmm.event.type.EventGlobal

class LayersFilterController : EventConsumer, EventSender {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(Event.LayersFilterController.FilterById::class.java, ::handleFilterById)
        consumeEvent(Event.LayersFilterController.ShowByType::class.java, ::handleShowByType)
        consumeEvent(Event.LayersFilterController.HideByType::class.java, ::handleHideByType)
        consumeEvent(Event.LayersFilterController.Fetch::class.java, ::handleFetch)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterById(event: Event<DmeItemIdArray, Unit>) {
        sendEvent(Event.EnvironmentController.Fetch { dme ->
            filteredTypes = dme.items.values.filter { event.body.contains(it.id) }.map { it.type }.toMutableSet()
            sendEvent(EventFrameController.Refresh())
            sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleShowByType(event: Event<DmeItemType, Unit>) {
        filteredTypes.removeIf { it.contains(event.body) }
        sendEvent(EventFrameController.Refresh())
        sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
    }

    private fun handleHideByType(event: Event<DmeItemType, Unit>) {
        sendEvent(Event.EnvironmentController.Fetch { dme ->
            filteredTypes.addAll(dme.items.values.filter { it.type.contains(event.body) }.map { it.type })
            sendEvent(EventFrameController.Refresh())
            sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
        })
    }

    private fun handleFetch(event: Event<Unit, Set<DmeItemType>>) {
        event.reply(filteredTypes)
    }

    private fun handleEnvironmentReset() {
        filteredTypes.clear()
        sendEvent(EventGlobal.LayersFilterRefreshed(filteredTypes))
    }
}
