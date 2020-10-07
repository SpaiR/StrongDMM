package strongdmm.ui.panel.tool_select

import strongdmm.event.EventBus
import strongdmm.event.type.service.TriggerToolsService
import strongdmm.service.tool.ToolType

class ViewController {
    fun doSelectTool(tool: ToolType) {
        EventBus.post(TriggerToolsService.ChangeTool(tool))
    }
}
