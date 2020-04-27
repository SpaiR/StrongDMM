package strongdmm.ui.panel.tool_select_panel

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window

class View(
    private val state: State
) {
    companion object {
        private const val POS_X: Float = 350f
        private const val POS_Y: Float = 30f

        private const val WIDTH: Float = 80f
        private const val HEIGHT: Float = 35f

        private const val TITLE: String = "Tool"
        private const val FLAGS: Int = ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize
    }

    lateinit var viewController: ViewController

    fun process() {
        WindowUtil.setNextPosAndSize(POS_X, POS_Y, WIDTH, HEIGHT)

        window(TITLE, FLAGS) {
            state.tools.forEach { tool ->
                val isToolActive = tool == state.activeTool

                if (isToolActive) {
                    pushStyleColor(ImGuiCol.Button, 0f, .5f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonHovered, 0f, .8f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonActive, 0f, .5f, 0f, 1f)
                }

                button(tool.toolName) {
                    viewController.doSelectTool(tool)
                }

                if (isItemHovered()) {
                    setTooltip(tool.toolDesc)
                }

                if (isToolActive) {
                    popStyleColor(3)
                }

                sameLine()
            }
        }
    }
}
