package strongdmm.ui.panel.variables_preview

import imgui.ImGui.*
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private const val POS_X: Float = 350f
        private const val RELATIVE_POS_Y: Float = 210f
        private const val RELATIVE_POS_Y_COLLAPSED: Float = 80f

        private const val WIDTH: Float = 300f
        private const val HEIGHT: Float = 195f
        private const val HEIGHT_COLLAPSED: Float = 65f

        private const val TITLE: String = "Variables Preview"
    }

    fun process() {
        if (state.selectedTileItem == null) {
            return
        }

        val isEmpty = state.selectedTileItem?.customVars == null

        if (isEmpty) {
            WindowUtil.setNextPosAndSize(POS_X, Window.windowHeight - RELATIVE_POS_Y_COLLAPSED, WIDTH, HEIGHT_COLLAPSED)
        } else {
            WindowUtil.setNextPosAndSize(POS_X, Window.windowHeight - RELATIVE_POS_Y, WIDTH, HEIGHT)
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
