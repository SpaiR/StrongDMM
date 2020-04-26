package strongdmm.ui.tool_select_panel

import strongdmm.controller.tool.ToolType
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.ActiveToolChanged::class.java, ::handleActiveToolChanged)
    }

    private fun handleActiveToolChanged(event: Event<ToolType, Unit>) {
        state.activeTool = event.body
    }
}
