package strongdmm.ui.panel.notification

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
        private const val TITLE: String = "notification_panel"
        private const val FLAGS: Int =
            ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isOpening() || viewController.isClosing()) {
            setNextWindowSize(state.notificationTextWidth, state.windowHeight)
        }

        if (state.notificationText == null && state.windowHeight <= 0) {
            return
        }

        setNextWindowPos((AppWindow.windowWidth - state.notificationTextWidth) / 2, POS_Y)

        pushStyleColor(ImGuiCol.WindowBg, getColorU32(ImGuiCol.MenuBarBg))
        pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)

        window(TITLE, FLAGS) {
            text(state.notificationText ?: "")
        }

        popStyleVar()
        popStyleColor()
    }
}
