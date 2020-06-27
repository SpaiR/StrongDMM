package strongdmm.ui.panel.coords

import imgui.ImGui.*
import imgui.ImVec2
import imgui.flag.ImGuiWindowFlags
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private const val TITLE: String = "coords_panel"
    }

    private val textSize: ImVec2 = ImVec2()

    fun process() {
        if (!state.isMapOpened) {
            return
        }

        setNextWindowPos(CoordsPanelUi.posX, CoordsPanelUi.posY, Window.windowCond)
        setNextWindowSize(CoordsPanelUi.width, CoordsPanelUi.height, Window.windowCond)

        window(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            val text = if (state.xMapMousePos == OUT_OF_BOUNDS || state.yMapMousePos == OUT_OF_BOUNDS) {
                "out of bound"
            } else {
                "X:%03d Y:%03d".format(state.xMapMousePos, state.yMapMousePos)
            }

            calcTextSize(textSize, text)
            setCursorPos((CoordsPanelUi.width - textSize.x) / 2, (CoordsPanelUi.height - textSize.y) / 2)
            text(text)
        }
    }
}
