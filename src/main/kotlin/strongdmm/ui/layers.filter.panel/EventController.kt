package strongdmm.ui.layers.filter.panel

import strongdmm.byond.dme.Dme
import strongdmm.event.DmeItemType
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerEnvironmentController
import strongdmm.event.type.ui.TriggerLayersFilterPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerLayersFilterPanelUi.Open::class.java, ::handleOpen)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }

    private fun handleEnvironmentReset() {
        state.currentEnvironment = null
        state.filteredTypesId.clear()
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        state.currentEnvironment = event.body
    }

    private fun handleLayersFilterRefreshed(event: Event<Set<DmeItemType>, Unit>) {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment {
            state.filteredTypesId.clear()
            it.items.values.forEach { dmeItem ->
                if (event.body.contains(dmeItem.type)) {
                    state.filteredTypesId.add(dmeItem.id)
                }
            }
        })
    }
}
