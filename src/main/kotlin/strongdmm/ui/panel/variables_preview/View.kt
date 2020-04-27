package strongdmm.ui.panel.variables_preview

import imgui.ImGui.*
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class View(
    private val state: State
) {
    companion object {
        private const val POS_X: Float = 350f
        private const val RELATIVE_POS_Y: Float = 210f

        private const val WIDTH: Float = 300f
        private const val HEIGHT: Float = 195f

        private const val TITLE: String = "Variables Preview"
    }

    fun process() {
        if (state.selectedTileItem == null) {
            return
        }

        WindowUtil.setNextPosAndSize(POS_X, AppWindow.windowHeight - RELATIVE_POS_Y, WIDTH, HEIGHT)

        window(TITLE) {
            if (state.selectedTileItem?.customVars == null) {
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
