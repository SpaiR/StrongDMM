package strongdmm.ui.panel.tool_select

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.COLOR_GREEN
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = LayoutManager.Top.Left.posX + LayoutManager.Top.Left.width + LayoutManager.ELEMENT_MARGIN
        private val posY: Float
            get() = LayoutManager.Top.Left.posY

        private const val TITLE: String = "Tool"
    }

    lateinit var viewController: ViewController

    fun process() {
        setNextWindowPos(posX, posY, Window.windowCond)

        window(TITLE, ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.AlwaysAutoResize) {
            state.tools.forEach { tool ->
                val isToolSelected = tool == state.selectedTool

                if (isToolSelected) {
                    pushStyleColor(ImGuiCol.Button, COLOR_GREEN)
                    pushStyleColor(ImGuiCol.ButtonHovered, COLOR_GREEN)
                    pushStyleColor(ImGuiCol.ButtonActive, COLOR_GREEN)
                }

                button(tool.toolName) {
                    viewController.doSelectTool(tool)
                }

                if (isItemHovered()) {
                    setTooltip(tool.toolDesc)
                }

                if (isToolSelected) {
                    popStyleColor(3)
                }

                sameLine()
            }
        }
    }
}
