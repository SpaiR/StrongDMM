package strongdmm.ui.panel.tool_select

import strongdmm.service.tool.ToolType

class State {
    val tools: Array<ToolType> = ToolType.values()
    var selectedTool: ToolType = ToolType.TILE
}
