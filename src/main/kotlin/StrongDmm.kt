import strongdmm.controller.EnvironmentController
import strongdmm.controller.MapController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.native.ImGuiWindow
import strongdmm.ui.CoordsPanelUi
import strongdmm.ui.MenuBarUi
import strongdmm.ui.OpenedMapsPanelUi
import strongdmm.ui.WindowTitleUi

class StrongDmm(title: String) : ImGuiWindow(title) {
    private val menuBarUi = MenuBarUi()
    private val coordsPanelUi = CoordsPanelUi()
    private val openedMapsPanelUi = OpenedMapsPanelUi()
    private val windowTitleUi = WindowTitleUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()

    override fun appLoop(windowWidth: Int, windowHeight: Int) {
        // UIs
        menuBarUi.process()
        coordsPanelUi.process(windowWidth, windowHeight)
        openedMapsPanelUi.process(windowWidth, windowHeight)

        // Controllers (SHOULD go after UI classes)
        canvasController.process(windowWidth, windowHeight)
    }

    companion object {
        const val TITLE: String = "StrongDMM"

        @JvmStatic
        fun main(args: Array<String>) {
            StrongDmm(TITLE).start()
        }
    }
}
