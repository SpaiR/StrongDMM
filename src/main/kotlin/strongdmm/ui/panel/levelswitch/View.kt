package strongdmm.ui.panel.levelswitch

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.COLOR_GREY
import strongdmm.util.imgui.ImGuiExt
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window
import strongdmm.util.imgui.ImGuiIconFA

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = LayoutManager.Bottom.Right.posX
        private val posY: Float
            get() = LayoutManager.Bottom.Right.posY - height - LayoutManager.ELEMENT_MARGIN

        private val width: Float
            get() = LayoutManager.Bottom.Right.width
        private val height: Float
            get() = LayoutManager.Bottom.Right.height

        private const val TITLE: String = "lavel_switch_panel"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isNotProcessable()) {
            return
        }

        ImGui.setNextWindowPos(posX, posY, Window.windowCond)
        ImGui.setNextWindowSize(width, height, Window.windowCond)

        imGuiBegin(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            state.selectedMap?.let { map ->
                if (map.zSelected == 1) {
                    showDisabledSwitch(ImGuiIconFA.CHEVRON_LEFT)
                } else {
                    if (ImGui.smallButton(ImGuiIconFA.CHEVRON_LEFT)) {
                        viewController.doDecreaseSelectedZ()
                    }

                    ImGuiExt.setItemHoveredTooltip("Prev Z level (Ctrl+Left Arrow)")
                }

                ImGui.sameLine()
                ImGui.text("Z:${map.zSelected}")
                ImGui.sameLine()

                if (map.zSelected == map.maxZ) {
                    showDisabledSwitch(ImGuiIconFA.CHEVRON_RIGHT)
                } else {
                    if (ImGui.smallButton(ImGuiIconFA.CHEVRON_RIGHT)) {
                        viewController.doIncreaseSelectedZ()
                    }

                    ImGuiExt.setItemHoveredTooltip("Next Z level (Ctrl+Right Arrow)")
                }
            }
        }
    }

    private fun showDisabledSwitch(switchSymbol: String) {
        ImGui.pushStyleColor(ImGuiCol.Button, COLOR_GREY)
        ImGui.pushStyleColor(ImGuiCol.ButtonActive, COLOR_GREY)
        ImGui.pushStyleColor(ImGuiCol.ButtonHovered, COLOR_GREY)
        ImGui.smallButton(switchSymbol)
        ImGui.popStyleColor(3)
    }
}
