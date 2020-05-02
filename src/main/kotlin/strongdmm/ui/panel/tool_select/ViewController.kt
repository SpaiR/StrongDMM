package strongdmm.ui.panel.tool_select

import strongdmm.event.EventHandler
import strongdmm.event.type.service.TriggerToolsService
import strongdmm.service.tool.ToolType

class ViewController : EventHandler {
    fun doSelectTool(tool: ToolType) {
        sendEvent(TriggerToolsService.ChangeTool(tool))
    }
}
