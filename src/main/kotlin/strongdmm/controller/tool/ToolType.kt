package strongdmm.controller.tool

import strongdmm.controller.tool.fill.FillComplexTool
import strongdmm.controller.tool.select.SelectComplexTool
import strongdmm.controller.tool.tile.TileComplexTool

enum class ToolType(
    val toolName: String,
    val toolDesc: String,
    val createTool: () -> Tool
) {
    TILE("T", """
    Tile (Key 1)
    ------------
    Click - Place selected object
    Ctrl+Click - Delete topmost object
    """.trimIndent(), { TileComplexTool() }),

    FILL("F", """
    Fill (Key 2)
    ------------
    Click & Drag - Fill the area with selected object
    Ctrl+[Click & Drag] - Delete all topmost objects in the area
    """.trimIndent(), { FillComplexTool() }),

    SELECT("S", """
    Select (Key 3)
    --------------
    Click & Drag - Select the area / Move selection with visible objects inside
    """.trimIndent(), { SelectComplexTool() })
}
