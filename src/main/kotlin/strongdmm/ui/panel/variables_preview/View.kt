package strongdmm.ui.panel.variables_preview

import imgui.ImGui
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.COLOR_LIME
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = LayoutManager.Bottom.Left.posX + LayoutManager.Bottom.Left.width + LayoutManager.ELEMENT_MARGIN
        private val posY: Float
            get() = Window.windowHeight - height - LayoutManager.ELEMENT_MARGIN
        private val posYcollapsed: Float
            get() = Window.windowHeight - heightCollapsed - LayoutManager.ELEMENT_MARGIN

        private val width: Float
            get() = 300f * Window.pointSize
        private val height: Float
            get() = 195f * Window.pointSize
        private val heightCollapsed: Float
            get() = 65f * Window.pointSize

        private const val TITLE: String = "Variables Preview"
    }

    fun process() {
        if (state.selectedTileItem == null) {
            return
        }

        val isEmpty = state.selectedTileItem?.customVars == null

        if (isEmpty) {
            ImGui.setNextWindowPos(posX, posYcollapsed, Window.windowCond)
            ImGui.setNextWindowSize(width, heightCollapsed, Window.windowCond)
        } else {
            ImGui.setNextWindowPos(posX, posY, Window.windowCond)
            ImGui.setNextWindowSize(width, height, Window.windowCond)
        }

        imGuiBegin("$TITLE##variables_preview_$isEmpty") {
            if (isEmpty) {
                ImGui.text("Empty (instance with initial vars)")
            } else {
                ImGui.columns(2)

                state.selectedTileItem!!.customVars!!.forEach { (name, value) ->
                    ImGui.textColored(COLOR_LIME, name)
                    ImGui.nextColumn()
                    ImGui.text(value)
                    ImGui.nextColumn()
                }
            }
        }
    }
}
