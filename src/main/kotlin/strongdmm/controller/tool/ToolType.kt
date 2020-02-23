package strongdmm.controller.tool

enum class ToolType(
    val toolName: String,
    val toolHelper: String,
    val createTool: () -> Tool
) {
    TILE("T", "Tile (Alt+1)\nClick - Place selected object\nCtrl+Click - Delete topmost object", { TileComplexTool() }),
    FILL("F", "Fill (Alt+2)\nDrag - Fill the area with selected object\nCtrl+Drag - Delete all topmost objects in the area", { FillComplexTool() })
}
