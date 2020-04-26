package strongdmm.ui.tool_select_panel

import strongdmm.controller.tool.ToolType

class State {
    val tools: Array<ToolType> = ToolType.values()
    var activeTool: ToolType = ToolType.TILE
}
