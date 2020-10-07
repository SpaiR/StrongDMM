package strongdmm.ui.panel.tool_select

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.service.tool.ToolType

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.SelectedToolChanged::class.java, ::handleSelectedToolChanged)
    }

    private fun handleSelectedToolChanged(event: Event<ToolType, Unit>) {
        state.selectedTool = event.body
    }
}
