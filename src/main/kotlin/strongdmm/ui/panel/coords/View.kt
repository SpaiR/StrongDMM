package strongdmm.ui.panel.coords

import imgui.ImGui.text
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private const val RELATIVE_POS_X: Float = 110f
        private const val RELATIVE_POS_Y: Float = 40f

        private const val WIDTH: Float = 87f
        private const val HEIGHT: Float = 10f

        private const val TITLE: String = "coords_panel"
    }

    fun process() {
        if (!state.isMapOpened) {
            return
        }

        WindowUtil.setNextPosAndSize(Window.windowWidth - RELATIVE_POS_X, Window.windowHeight - RELATIVE_POS_Y, WIDTH, HEIGHT)

        window(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            if (state.xMapMousePos == OUT_OF_BOUNDS || state.yMapMousePos == OUT_OF_BOUNDS) {
                text("out of bound")
            } else {
                text("X:%03d Y:%03d".format(state.xMapMousePos, state.yMapMousePos))
            }
        }
    }
}
