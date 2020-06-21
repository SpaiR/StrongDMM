package strongdmm.ui.panel.opened_maps

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import strongdmm.ui.UiConstant
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.util.icons.ICON_FA_TIMES
import strongdmm.util.imgui.*
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = Window.windowWidth - width - UiConstant.ELEMENT_MARGIN
        private val posY: Float
            get() = EnvironmentTreePanelUi.posY

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
