package strongdmm.ui.panel.toolselect

import strongdmm.event.EventBus
import strongdmm.event.service.TriggerToolsService
import strongdmm.service.tool.ToolType

class ViewController {
    fun doSelectTool(tool: ToolType) {
        EventBus.post(TriggerToolsService.ChangeTool(tool))
    }
}
