package strongdmm.event.service

import strongdmm.event.Event
import strongdmm.service.tool.ToolType

abstract class ReactionToolsService {
    class SelectedToolChanged(body: ToolType) : Event<ToolType, Unit>(body, null)
}
