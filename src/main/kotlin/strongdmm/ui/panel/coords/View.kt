package strongdmm.ui.panel.coords

import imgui.ImGui.*
import imgui.ImVec2
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
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

        setNextWindowPos(LayoutManager.Bottom.Right.posX, LayoutManager.Bottom.Right.posY, Window.windowCond)
        setNextWindowSize(LayoutManager.Bottom.Right.width, LayoutManager.Bottom.Right.height, Window.windowCond)

        window(TITLE, ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            val text = if (state.xMapMousePos == OUT_OF_BOUNDS || state.yMapMousePos == OUT_OF_BOUNDS) {
                "out of bound"
            } else {
                "X:%03d Y:%03d".format(state.xMapMousePos, state.yMapMousePos)
            }

            calcTextSize(textSize, text)
            setCursorPos((LayoutManager.Bottom.Right.width - textSize.x) / 2, (LayoutManager.Bottom.Right.height - textSize.y) / 2)
            text(text)
        }
    }
}
