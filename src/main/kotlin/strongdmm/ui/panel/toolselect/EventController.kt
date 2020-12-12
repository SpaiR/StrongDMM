package strongdmm.ui.panel.toolselect

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionToolsService
import strongdmm.service.tool.ToolType

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionToolsService.SelectedToolChanged::class.java, ::handleSelectedToolChanged)
    }

    private fun handleSelectedToolChanged(event: Event<ToolType, Unit>) {
        state.selectedTool = event.body
    }
}
