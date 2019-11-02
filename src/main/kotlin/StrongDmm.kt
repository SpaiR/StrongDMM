import strongdmm.controller.EnvironmentController
import strongdmm.controller.InputMouseController
import strongdmm.controller.MapController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.native.ImGuiWindow
import strongdmm.ui.MenuBarUi

class StrongDmm : ImGuiWindow() {
    private val menuBarUi = MenuBarUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()
    private val inputMouseController = InputMouseController()

    override fun guiLoop() {
        menuBarUi.process()
    }

    override fun canvasLoop(windowWidth: Int, windowHeight: Int) {
        canvasController.process(windowWidth, windowHeight)
    }

    override fun inputLoop() {
        inputMouseController.process()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            StrongDmm().start()
        }
    }
}
