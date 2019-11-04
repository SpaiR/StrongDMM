import strongdmm.controller.EnvironmentController
import strongdmm.controller.MapController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.native.ImGuiWindow
import strongdmm.ui.CoordsPanelUi
import strongdmm.ui.MenuBarUi

class StrongDmm : ImGuiWindow() {
    private val menuBarUi = MenuBarUi()
    private val coordsPanelUi = CoordsPanelUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()

    override fun appLoop(windowWidth: Int, windowHeight: Int) {
        // UIs
        menuBarUi.process()
        coordsPanelUi.process(windowWidth, windowHeight)

        // Controllers (SHOULD go after UI classes)
        canvasController.process(windowWidth, windowHeight)
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            StrongDmm().start()
        }
    }
}
