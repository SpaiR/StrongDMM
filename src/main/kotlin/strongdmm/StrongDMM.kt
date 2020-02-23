package strongdmm

import strongdmm.controller.EnvironmentController
import strongdmm.controller.InstanceController
import strongdmm.controller.LayersFilterController
import strongdmm.controller.action.ActionController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.controller.map.MapHolderController
import strongdmm.controller.map.MapModifierController
import strongdmm.controller.tool.ToolsController
import strongdmm.ui.*
import strongdmm.ui.search.SearchResultPanelUi
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
    private val instanceLocatorPanelUi = InstanceLocatorPanelUi()
    private val searchResultPanelUi = SearchResultPanelUi()
    private val layersFilterPanelUi = LayersFilterPanelUi()
    private val toolSelectPanelUi = ToolSelectPanelUi()

    private val environmentController = EnvironmentController()
    private val mapController = MapHolderController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()
    private val actionController = ActionController()
    private val instanceController = InstanceController()
    private val mapModifierController = MapModifierController()
    private val layersFilterController = LayersFilterController()
    private val toolsController = ToolsController()

    init {
        instanceLocatorPanelUi.postInit()
    }

    override fun appLoop() {
        // UIs
        menuBarUi.process()
        coordsPanelUi.process()
        openedMapsPanelUi.process()
        availableMapsDialogUi.process()
        tilePopupUi.process()
        editVarsDialogUi.process()
        environmentTreePanelUi.process()
        objectPanelUi.process()
        instanceLocatorPanelUi.process()
        searchResultPanelUi.process()
        layersFilterPanelUi.process()
        toolSelectPanelUi.process()

        // Controllers
        canvasController.process()
    }

    companion object {
        const val TITLE: String = "StrongDMM"

        @JvmStatic
        fun main(args: Array<String>) {
            StrongDMM(TITLE).start()
        }
    }
}
