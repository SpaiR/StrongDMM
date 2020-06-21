package strongdmm.ui.panel.level_switch

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import strongdmm.ui.UiConstant
import strongdmm.ui.panel.coords.CoordsPanelUi
import strongdmm.util.icons.ICON_FA_CHEVRON_LEFT
import strongdmm.util.icons.ICON_FA_CHEVRON_RIGHT
import strongdmm.util.imgui.GREY32
import strongdmm.util.imgui.setItemHoveredTooltip
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = CoordsPanelUi.posX
        private val posY: Float
            get() = CoordsPanelUi.posY - height - UiConstant.ELEMENT_MARGIN

        private val width: Float
            get() = CoordsPanelUi.width
        private val height: Float
            get() = CoordsPanelUi.height

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

                    setItemHoveredTooltip("Prev Z level (Ctrl+Left Arrow)")
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
