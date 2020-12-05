package strongdmm.ui.panel.opened_maps

import imgui.ImGui
import imgui.flag.ImGuiCol
import strongdmm.ui.LayoutManager
import strongdmm.util.icons.ICON_FA_TIMES
import strongdmm.util.imgui.*
import strongdmm.application.window.Window

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

        ImGui.setNextWindowPos(posX, posY, Window.windowCond)
        ImGui.setNextWindowSize(width, height, Window.windowCond)

        val isSelectedMapModified = viewController.isModifiedMap(state.selectedMap!!)

        if (isSelectedMapModified) {
            ImGui.pushStyleColor(ImGuiCol.Text, COLOR_GOLD)
        }

        if (ImGui.begin("${state.selectedMap!!.mapName}###opened_maps")) {
            if (isSelectedMapModified) {
                ImGui.popStyleColor()
            }

            state.providedOpenedMaps.toTypedArray().forEach { map ->
                imGuiWithStyleColor(ImGuiCol.ButtonHovered, COLOR_RED) {
                    imGuiSmallButton("$ICON_FA_TIMES##close_map_${map.mapPath.readable}") {
                        viewController.doCloseMap(map)
                    }
                }

                ImGui.sameLine()

                val isMapModified = viewController.isModifiedMap(map)

                if (isMapModified) {
                    ImGui.pushStyleColor(ImGuiCol.Text, COLOR_GOLD)
                }

                if (ImGui.selectable("${map.mapName}##open_${map.mapPath.absolute}", viewController.isSelectedMap(map))) {
                    viewController.doOpenMap(map)
                }

                if (isMapModified) {
                    ImGui.popStyleColor()
                }

                ImGuiExt.setItemHoveredTooltip(map.mapPath.readable)
            }
        } else if (isSelectedMapModified) {
            ImGui.popStyleColor()
        }

        ImGui.end()
    }
}
