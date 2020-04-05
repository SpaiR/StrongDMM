package strongdmm

import strongdmm.controller.*
import strongdmm.controller.action.ActionController
import strongdmm.controller.canvas.CanvasController
import strongdmm.controller.frame.FrameController
import strongdmm.controller.map.MapHolderController
import strongdmm.controller.map.MapModifierController
import strongdmm.controller.preferences.PreferencesController
import strongdmm.controller.recent.RecentFilesController
import strongdmm.controller.shortcut.ShortcutController
import strongdmm.controller.tool.ToolsController
import strongdmm.ui.*
import strongdmm.ui.search.SearchResultPanelUi
import strongdmm.ui.vars.EditVarsDialogUi
import strongdmm.window.AppWindow
import java.nio.file.Path
import java.nio.file.Paths

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
    private val levelSwitchPanelUi = LevelSwitchPanelUi()
    private val preferencesPanelUi = PreferencesPanelUi()

    private val environmentController = EnvironmentController()
    private val mapHolderController = MapHolderController()
    private val mapModifierController = MapModifierController()
    private val canvasController = CanvasController()
    private val frameController = FrameController()
    private val actionController = ActionController()
    private val instanceController = InstanceController()
    private val layersFilterController = LayersFilterController()
    private val toolsController = ToolsController()
    private val shortcutController = ShortcutController()
    private val clipboardController = ClipboardController()
    private val tileItemController = TileItemController()
    private val recentFilesController = RecentFilesController()
    private val preferencesController = PreferencesController()

    init {
        ensureHomeDirExists()

        instanceLocatorPanelUi.postInit()
        mapHolderController.postInit()
        frameController.postInit()
        actionController.postInit()
        canvasController.postInit()
        recentFilesController.postInit()
        preferencesController.postInit()
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
        levelSwitchPanelUi.process()
        preferencesPanelUi.process()

        // Controllers
        canvasController.process()
        shortcutController.process()
    }

    private fun ensureHomeDirExists() {
        homeDir.toFile().mkdirs()
    }

    companion object {
        const val TITLE: String = "StrongDMM"

        val homeDir: Path = Paths.get(System.getProperty("user.home")).resolve(".strongdmm")

        @JvmStatic
        fun main(args: Array<String>) {
            StrongDMM(TITLE).start()
        }
    }
}
