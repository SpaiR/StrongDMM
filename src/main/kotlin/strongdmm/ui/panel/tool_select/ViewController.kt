package strongdmm.ui.panel.tool_select_panel

import strongdmm.controller.tool.ToolType
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.TriggerToolsController

class ViewController : EventHandler {
    fun doSelectTool(tool: ToolType) {
        sendEvent(TriggerToolsController.ChangeTool(tool))
    }
}
