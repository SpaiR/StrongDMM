import strongdmm.controller.EnvironmentController
import strongdmm.controller.MapController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.native.ImGuiWindow
import strongdmm.ui.*

class StrongDmm(title: String) : ImGuiWindow(title) {
    private val menuBarUi = MenuBarUi()
    private val coordsPanelUi = CoordsPanelUi()
    private val openedMapsPanelUi = OpenedMapsPanelUi()
    private val windowTitleUi = WindowTitleUi()
    private val availableMapsDialogUi = AvailableMapsDialogUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()

    override fun appLoop(windowWidth: Int, windowHeight: Int) {
        // Controllers
        canvasController.process(windowWidth, windowHeight)

        // UIs
        menuBarUi.process()
        coordsPanelUi.process(windowWidth, windowHeight)
        openedMapsPanelUi.process(windowWidth, windowHeight)
        availableMapsDialogUi.process()
    }

    companion object {
        const val TITLE: String = "StrongDMM"

        @JvmStatic
        fun main(args: Array<String>) {
            StrongDmm(TITLE).start()
        }
    }
}
