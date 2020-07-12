package strongdmm.ui.panel.opened_maps

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import strongdmm.ui.LayoutManager
import strongdmm.util.icons.ICON_FA_TIMES
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = Window.windowWidth - width - LayoutManager.ELEMENT_MARGIN
        private val posY: Float
            get() = LayoutManager.Top.Left.posY

        private val width: Float
            get() = 150f * Window.pointSize
        private val height: Float
            get() = 150f * Window.pointSize
    }

    lateinit var viewController: ViewController

    fun process() {
        if (state.providedOpenedMaps.isEmpty() || state.selectedMap == null) {
            return
        }

        setNextWindowPos(posX, posY, Window.windowCond)
        setNextWindowSize(width, height, Window.windowCond)

        val isSelectedMapModified = viewController.isModifiedMap(state.selectedMap!!)

        if (isSelectedMapModified) {
            pushStyleColor(ImGuiCol.Text, COLOR_GOLD)
        }

        if (begin("${state.selectedMap!!.mapName}###opened_maps")) {
            if (isSelectedMapModified) {
                popStyleColor()
            }

            state.providedOpenedMaps.toTypedArray().forEach { map ->
                withStyleColor(ImGuiCol.ButtonHovered, COLOR_RED) {
                    smallButton("$ICON_FA_TIMES##close_map_${map.mapPath.readable}") {
                        viewController.doCloseMap(map)
                    }
                }

                sameLine()

                val isMapModified = viewController.isModifiedMap(map)

                if (isMapModified) {
                    pushStyleColor(ImGuiCol.Text, COLOR_GOLD)
                }

                if (selectable("${map.mapName}##open_${map.mapPath.absolute}", viewController.isSelectedMap(map))) {
                    viewController.doOpenMap(map)
                }

                if (isMapModified) {
                    popStyleColor()
                }

                ImGuiExt.setItemHoveredTooltip(map.mapPath.readable)
            }
        } else if (isSelectedMapModified) {
            popStyleColor()
        }

        end()
    }
}
