package strongdmm.ui.panel.opened_maps

import imgui.ImGui.sameLine
import imgui.ImGui.selectable
import imgui.enums.ImGuiCol
import strongdmm.util.icons.ICON_FA_TIMES
import strongdmm.util.imgui.*
import strongdmm.window.AppWindow

class View(
    private val state: State
) {
    companion object {
        private const val RELATIVE_POS_X: Float = 160f
        private const val POS_Y: Float = 30f

        private const val WIDTH: Float = 150f
        private const val HEIGHT: Float = 150f
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.providedOpenedMaps.isEmpty() || state.selectedMap == null) {
            return
        }

        WindowUtil.setNextPosAndSize(AppWindow.windowWidth - RELATIVE_POS_X, POS_Y, WIDTH, HEIGHT)

        window("${viewController.getMapName(state.selectedMap!!)}###opened_maps") {
            state.providedOpenedMaps.toTypedArray().forEach { map ->
                withStyleColor(ImGuiCol.ButtonHovered, RED32) {
                    smallButton("$ICON_FA_TIMES##close_map_${map.mapPath.readable}") {
                        viewController.doCloseMap(map)
                    }
                }

                sameLine()

                if (selectable(viewController.getMapName(map), state.selectedMap === map)) {
                    viewController.doOpenMap(map)
                }

                setItemHoveredTooltip(map.mapPath.readable)
            }
        }
    }
}
