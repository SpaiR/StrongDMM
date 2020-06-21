package strongdmm.ui.panel.progress

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiStyleVar
import imgui.enums.ImGuiWindowFlags
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posY: Float
            get() = EnvironmentTreePanelUi.posY

        private val height: Float
            get() = 30f * Window.pointSize

        private const val TITLE: String = "progress_bar_panel"

        private val progressBarVisual = arrayOf("·   ", " ·  ", "  · ", "   ·", "   ·", "  · ", " ·  ", "·   ")
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isOpening() || viewController.isClosing()) {
            setNextWindowSize(state.windowWidth, height)
        }

        if (state.progressText == null && state.windowWidth <= 0) {
            return
        }

        setNextWindowPos((Window.windowWidth - state.progressTextWidth) / 2, posY)

        pushStyleColor(ImGuiCol.WindowBg, getColorU32(ImGuiCol.MenuBarBg))
        pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)

        window(TITLE, ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar) {
            if (state.progressText != null) {
                val count = (getTime() / 0.125).toInt() and 7
                text("${progressBarVisual[count]} ${state.progressText} ${progressBarVisual[count]}")
            }
        }

        popStyleVar()
        popStyleColor()
    }
}
