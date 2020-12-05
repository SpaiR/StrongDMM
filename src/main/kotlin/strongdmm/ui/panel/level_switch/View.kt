package strongdmm.ui.panel.level_switch

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.icons.ICON_FA_CHEVRON_LEFT
import strongdmm.util.icons.ICON_FA_CHEVRON_RIGHT
import strongdmm.util.imgui.COLOR_GREY
import strongdmm.util.imgui.ImGuiExt
import strongdmm.util.imgui.window
import strongdmm.application.window.Window

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

        setNextWindowPos(posX, posY, Window.windowCond)
        setNextWindowSize(width, height, Window.windowCond)

        window(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            state.selectedMap?.let { map ->
                if (map.zSelected == 1) {
                    showDisabledSwitch(ICON_FA_CHEVRON_LEFT)
                } else {
                    if (smallButton(ICON_FA_CHEVRON_LEFT)) {
                        viewController.doDecreaseSelectedZ()
                    }

                    ImGuiExt.setItemHoveredTooltip("Prev Z level (Ctrl+Left Arrow)")
                }

                sameLine()
                text("Z:${map.zSelected}")
                sameLine()

                if (map.zSelected == map.maxZ) {
                    showDisabledSwitch(ICON_FA_CHEVRON_RIGHT)
                } else {
                    if (smallButton(ICON_FA_CHEVRON_RIGHT)) {
                        viewController.doIncreaseSelectedZ()
                    }

                    ImGuiExt.setItemHoveredTooltip("Next Z level (Ctrl+Right Arrow)")
                }
            }
        }
    }

    private fun showDisabledSwitch(switchSymbol: String) {
        pushStyleColor(ImGuiCol.Button, COLOR_GREY)
        pushStyleColor(ImGuiCol.ButtonActive, COLOR_GREY)
        pushStyleColor(ImGuiCol.ButtonHovered, COLOR_GREY)
        smallButton(switchSymbol)
        popStyleColor(3)
    }
}
