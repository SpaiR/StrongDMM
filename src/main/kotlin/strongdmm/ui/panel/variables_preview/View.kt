package strongdmm.ui.panel.variables_preview

import imgui.ImGui.*
import strongdmm.ui.UiConstant
import strongdmm.ui.panel.objects.ObjectsPanelUi
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posX: Float
            get() = ObjectsPanelUi.posX + ObjectsPanelUi.width + UiConstant.ELEMENT_MARGIN
        private val posY: Float
            get() = Window.windowHeight - height - UiConstant.ELEMENT_MARGIN
        private val posYcollapsed: Float
            get() = Window.windowHeight - heightCollapsed - UiConstant.ELEMENT_MARGIN

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
            setNextWindowPos(posX, posYcollapsed, Window.windowCond)
            setNextWindowSize(width, heightCollapsed, Window.windowCond)
        } else {
            setNextWindowPos(posX, posY, Window.windowCond)
            setNextWindowSize(width, height, Window.windowCond)
        }

        window("$TITLE##variables_preview_$isEmpty") {
            if (isEmpty) {
                text("Empty (instance with initial vars)")
            } else {
                columns(2)

                state.selectedTileItem!!.customVars!!.forEach { (name, value) ->
                    textColored(0f, 1f, 0f, 1f, name)
                    nextColumn()
                    text(value)
                    nextColumn()
                }
            }
        }
    }
}
