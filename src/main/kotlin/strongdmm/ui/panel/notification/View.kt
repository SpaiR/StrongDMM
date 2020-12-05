package strongdmm.ui.panel.notification

import imgui.ImGui.*
import imgui.flag.ImGuiCol
import imgui.flag.ImGuiStyleVar
import imgui.flag.ImGuiWindowFlags
import strongdmm.ui.LayoutManager
import strongdmm.util.imgui.window
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val posY: Float
            get() = LayoutManager.Top.Left.posY

        private const val TITLE: String = "notification_panel"

        private const val FLAGS: Int = ImGuiWindowFlags.AlwaysAutoResize or ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoScrollbar
    }

    lateinit var viewController: ViewController

    fun process() {
        if (viewController.isOpening() || viewController.isClosing()) {
            setNextWindowSize(state.notificationTextWidth, state.windowHeight)
        }

        if (state.notificationText == null && state.windowHeight <= 0) {
            return
        }

        setNextWindowPos((Window.windowWidth - state.notificationTextWidth) / 2, posY)

        pushStyleColor(ImGuiCol.WindowBg, getColorU32(ImGuiCol.MenuBarBg))
        pushStyleVar(ImGuiStyleVar.WindowBorderSize, 0f)

        window(TITLE, FLAGS) {
            text(state.notificationText ?: "")
        }

        popStyleVar()
        popStyleColor()
    }
}
