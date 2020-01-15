package strongdmm

import strongdmm.controller.InstanceController
import strongdmm.controller.MapController
import strongdmm.controller.action.ActionController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.environment.EnvironmentController
import strongdmm.controller.frame.FrameController
import strongdmm.ui.*
import strongdmm.ui.vars.EditVarsDialogUi
import strongdmm.window.AppWindow

class StrongDMM(title: String) : AppWindow(title) {
    private val menuBarUi = MenuBarUi()
    private val coordsPanelUi = CoordsPanelUi()
    private val openedMapsPanelUi = OpenedMapsPanelUi()
    private val windowTitleUi = WindowTitleUi()
    private val availableMapsDialogUi = AvailableMapsDialogUi()
    private val tilePopupUi = TilePopupUi()
    private val editVarsDialogUi = EditVarsDialogUi()
    private val environmentTreePanelUi = EnvironmentTreePanelUi()
    private val objectPanelUi = ObjectPanelUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()
    private val actionController = ActionController()
    private val instanceController = InstanceController()

    override fun appLoop(windowWidth: Int, windowHeight: Int) {
        // UIs
        menuBarUi.process()
        coordsPanelUi.process(windowWidth, windowHeight)
        openedMapsPanelUi.process(windowWidth)
        availableMapsDialogUi.process()
        tilePopupUi.process()
        editVarsDialogUi.process(windowWidth, windowHeight)
        environmentTreePanelUi.process()
        objectPanelUi.process()

        // Controllers
        canvasController.process(windowWidth, windowHeight)
    }

    companion object {
        const val TITLE: String = "StrongDMM"

        @JvmStatic
        fun main(args: Array<String>) {
            StrongDMM(TITLE).start()
        }
    }
}
