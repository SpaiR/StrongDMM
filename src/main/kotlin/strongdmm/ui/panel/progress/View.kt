package strongdmm.ui.panel.progress

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiStyleVar
import imgui.enums.ImGuiWindowFlags
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class View(
    private val state: State
) {
    companion object {
        private const val POS_Y: Float = 30f
        private const val HEIGHT: Float = 30f
        private const val TITLE: String = "progress_bar_panel"
        private const val FLAGS: Int = ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar

        private val progressBarVisual = arrayOf("·   ", " ·  ", "  · ", "   ·", "   ·", "  · ", " ·  ", "·   ")
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isOpening() || viewController.isClosing()) {
            setNextWindowSize(state.windowWidth, HEIGHT)
        }

        if (state.progressText == null && state.windowWidth <= 0) {
            return
        }

        setNextWindowPos((AppWindow.windowWidth - state.progressTextWidth) / 2, POS_Y)

        pushStyleColor(ImGuiCol.WindowBg, getColorU32(ImGuiCol.MenuBarBg))
        pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)

        window(TITLE, FLAGS) {
            if (state.progressText != null) {
                val count = (getTime() / 0.125).toInt() and 7
                text("${progressBarVisual[count]} ${state.progressText} ${progressBarVisual[count]}")
            }
        }

        popStyleVar()
        popStyleColor()
    }
}
