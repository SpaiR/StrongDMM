package strongdmm.ui.panel.unknown_types_panel

import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.ui.TriggerUnknownTypesPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerUnknownTypesPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen(event: Event<List<Pair<MapPos, String>>, Unit>) {
        state.unknownTypes = event.body
        state.isOpened.set(true)
    }
}
