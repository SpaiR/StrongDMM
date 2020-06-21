package strongdmm.ui.panel.tool_select

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import strongdmm.ui.UiConstant
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = EnvironmentTreePanelUi.posX + EnvironmentTreePanelUi.width + UiConstant.ELEMENT_MARGIN
        private val posY: Float
            get() = EnvironmentTreePanelUi.posY

        private const val TITLE: String = "Tool"
    }

    lateinit var viewController: ViewController

    fun process() {
        setNextWindowPos(posX, posY, Window.windowCond)

        window(TITLE, ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.AlwaysAutoResize) {
            state.tools.forEach { tool ->
                val isToolSelected = tool == state.selectedTool

                if (isToolSelected) {
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

                if (isToolSelected) {
                    popStyleColor(3)
                }

                sameLine()
            }
        }
    }
}
