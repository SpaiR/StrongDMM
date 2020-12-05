package strongdmm.service.tool

import strongdmm.service.tool.fill.FillComplexTool
import strongdmm.service.tool.select.SelectComplexTool
import strongdmm.service.tool.tile.TileComplexTool
import strongdmm.util.imgui.ImGuiIconFA

enum class ToolType(
    val toolName: String,
    val toolDesc: String,
    val createTool: () -> Tool
) {
    TILE(ImGuiIconFA.PLUS, """
    Tile (Key 1)
    ------------
    Click - Place selected object
    Ctrl+Click - Delete topmost object
    """.trimIndent(), { TileComplexTool() }),

    FILL(ImGuiIconFA.BORDER_ALL, """
    Fill (Key 2)
    ------------
    Click & Drag - Fill the area with selected object
    Ctrl+[Click & Drag] - Delete all topmost objects in the area
    """.trimIndent(), { FillComplexTool() }),

    SELECT(ImGuiIconFA.BORDER_STYLE, """
    Select (Key 3)
    --------------
    Click & Drag - Select the area / Move selection with visible objects inside
    """.trimIndent(), { SelectComplexTool() })
}
