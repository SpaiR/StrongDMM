package strongdmm.controller.tool

enum class ToolType(
    val toolName: String,
    val toolHelper: String,
    val createTool: () -> Tool
) {
    ADD("A", "Add (Alt+1)\nClick - Place selected object\nCtrl+Click - Delete topmost object", { AddDeleteTool() })
}
