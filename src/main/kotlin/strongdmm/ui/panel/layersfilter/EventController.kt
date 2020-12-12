package strongdmm.ui.panel.layersfilter

import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionLayersFilterService
import strongdmm.event.service.TriggerEnvironmentService
import strongdmm.event.ui.TriggerLayersFilterPanelUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerLayersFilterPanelUi.Open::class.java, ::handleOpen)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ReactionLayersFilterService.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)
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

    private fun handleLayersFilterRefreshed(event: Event<Set<String>, Unit>) {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            state.filteredTypesId.clear()
            it.items.values.forEach { dmeItem ->
                if (event.body.contains(dmeItem.type)) {
                    state.filteredTypesId.add(dmeItem.id)
                }
            }
        })
    }
}
