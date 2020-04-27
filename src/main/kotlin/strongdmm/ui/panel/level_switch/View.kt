package strongdmm.ui.panel.level_switch

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.GREY32
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.setItemHoveredTooltip
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class View(
    private val state: State
) {
    companion object {
        private const val RELATIVE_POS_X: Float = 100f
        private const val RELATIVE_POS_Y: Float = 75f

        private const val WIDTH: Float = 90f
        private const val HEIGHT: Float = 10f

        private const val TITLE: String = "lavel_switch_panel"
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isNotProcessable()) {
            return
        }

        WindowUtil.setNextPosAndSize(AppWindow.windowWidth - RELATIVE_POS_X, AppWindow.windowHeight - RELATIVE_POS_Y, WIDTH, HEIGHT)

        window(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            state.selectedMap?.let { map ->
                if (map.zActive == 1) {
                    showDisabledSwitch("<")
                } else {
                    if (smallButton("<")) {
                        viewController.doDecreaseActiveZ()
                    }

                    setItemHoveredTooltip("Prev Z level (Ctrl+Left Arrow)")
                }

                sameLine()
                text("Z:${map.zActive}")
                sameLine()

                if (map.zActive == map.maxZ) {
                    showDisabledSwitch(">")
                } else {
                    if (smallButton(">")) {
                        viewController.doIncreaseActiveZ()
                    }

                    setItemHoveredTooltip("Next Z level (Ctrl+Right Arrow)")
                }
            }
        }
    }

    private fun showDisabledSwitch(switchSymbol: String) {
        pushStyleColor(ImGuiCol.Button, GREY32)
        pushStyleColor(ImGuiCol.ButtonActive, GREY32)
        pushStyleColor(ImGuiCol.ButtonHovered, GREY32)
        smallButton(switchSymbol)
        popStyleColor(3)
    }
}
