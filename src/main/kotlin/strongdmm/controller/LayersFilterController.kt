package strongdmm.controller

import strongdmm.event.*
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.controller.EventEnvironmentController
import strongdmm.event.type.controller.EventFrameController
import strongdmm.event.type.controller.EventLayersFilterController

class LayersFilterController : EventConsumer, EventSender {
    private var filteredTypes: MutableSet<String> = HashSet()

    init {
        consumeEvent(EventLayersFilterController.FilterById::class.java, ::handleFilterById)
        consumeEvent(EventLayersFilterController.ShowByType::class.java, ::handleShowByType)
        consumeEvent(EventLayersFilterController.HideByType::class.java, ::handleHideByType)
        consumeEvent(EventLayersFilterController.Fetch::class.java, ::handleFetch)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
    }

    private fun handleFilterById(event: Event<DmeItemIdArray, Unit>) {
        sendEvent(EventEnvironmentController.Fetch { dme ->
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
        sendEvent(EventEnvironmentController.Fetch { dme ->
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
