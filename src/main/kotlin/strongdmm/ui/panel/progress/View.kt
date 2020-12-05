package strongdmm.ui.panel.progress

import imgui.ImGui
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posY: Float
            get() = LayoutManager.Top.Left.posY

        private val height: Float
            get() = 30f * Window.pointSize

        private const val TITLE: String = "progress_bar_panel"

        private val progressBarVisual = arrayOf("·   ", " ·  ", "  · ", "   ·", "   ·", "  · ", " ·  ", "·   ")
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isOpening() || viewController.isClosing()) {
            ImGui.setNextWindowSize(state.windowWidth, height)
        }

        if (state.progressText == null && state.windowWidth <= 0) {
            return
        }

        ImGui.setNextWindowPos((Window.windowWidth - state.progressTextWidth) / 2, posY)

        ImGui.pushStyleColor(ImGuiCol.WindowBg, ImGui.getColorU32(ImGuiCol.MenuBarBg))
        ImGui.pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)

        imGuiBegin(TITLE, ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar) {
            if (state.progressText != null) {
                val count = (ImGui.getTime() / 0.125).toInt() and 7
                ImGui.text("${progressBarVisual[count]} ${state.progressText} ${progressBarVisual[count]}")
            }
        }

        ImGui.popStyleVar()
        ImGui.popStyleColor()
    }
}
