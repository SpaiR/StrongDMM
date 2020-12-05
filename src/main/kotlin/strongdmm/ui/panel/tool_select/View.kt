package strongdmm.ui.panel.tool_select

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.COLOR_GREEN
import strongdmm.util.imgui.imGuiButton
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window

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
        ImGui.setNextWindowPos(posX, posY, Window.windowCond)

        imGuiBegin(TITLE, ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.AlwaysAutoResize) {
            state.tools.forEach { tool ->
                val isToolSelected = tool == state.selectedTool

                if (isToolSelected) {
                    ImGui.pushStyleColor(ImGuiCol.Button, COLOR_GREEN)
                    ImGui.pushStyleColor(ImGuiCol.ButtonHovered, COLOR_GREEN)
                    ImGui.pushStyleColor(ImGuiCol.ButtonActive, COLOR_GREEN)
                }

                imGuiButton(tool.toolName) {
                    viewController.doSelectTool(tool)
                }

                if (ImGui.isItemHovered()) {
                    ImGui.setTooltip(tool.toolDesc)
                }

                if (isToolSelected) {
                    ImGui.popStyleColor(3)
                }

                ImGui.sameLine()
            }
        }
    }
}
