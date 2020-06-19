package strongdmm.ui.panel.opened_maps

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import strongdmm.util.icons.ICON_FA_TIMES
import strongdmm.util.imgui.*
import strongdmm.window.Window

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

        WindowUtil.setNextPosAndSize(Window.windowWidth - RELATIVE_POS_X, POS_Y, WIDTH, HEIGHT)

        val isSelectedMapModified = viewController.isModifiedMap(state.selectedMap!!)

        if (isSelectedMapModified) {
            pushStyleColor(ImGuiCol.Text, 1f, .84f, 0f, 1f)
        }

        if (begin("${state.selectedMap!!.mapName}###opened_maps")) {
            if (isSelectedMapModified) {
                popStyleColor()
            }

            state.providedOpenedMaps.toTypedArray().forEach { map ->
                withStyleColor(ImGuiCol.ButtonHovered, RED32) {
                    smallButton("$ICON_FA_TIMES##close_map_${map.mapPath.readable}") {
                        viewController.doCloseMap(map)
                    }
                }

                sameLine()

                val isMapModified = viewController.isModifiedMap(map)

                if (isMapModified) {
                    pushStyleColor(ImGuiCol.Text, 1f, .84f, 0f, 1f)
                }

                if (selectable(map.mapName, viewController.isSelectedMap(map))) {
                    viewController.doOpenMap(map)
                }

                if (isMapModified) {
                    popStyleColor()
                }

                setItemHoveredTooltip(map.mapPath.readable)
            }
        } else if (isSelectedMapModified) {
            popStyleColor()
        }

        end()
    }
}
