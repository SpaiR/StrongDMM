package strongdmm.ui.panel.tool_select

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.service.tool.ToolType

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.SelectedToolChanged::class.java, ::handleSelectedToolChanged)
    }

    private fun handleSelectedToolChanged(event: Event<ToolType, Unit>) {
        state.selectedTool = event.body
    }
}
