package strongdmm

import org.slf4j.LoggerFactory
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
import strongdmm.ui.panel.about.AboutPanelUi
import strongdmm.ui.dialog.available_maps.AvailableMapsDialogUi
import strongdmm.ui.panel.changelog.ChangelogPanelUi
import strongdmm.ui.dialog.close_map.CloseMapDialogUi
import strongdmm.ui.dialog.set_map_size.SetMapSizeDialogUi
import strongdmm.ui.panel.coords.CoordsPanelUi
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.ui.panel.instance_locator.InstanceLocatorPanelUi
import strongdmm.ui.panel.layers_filter.LayersFilterPanelUi
import strongdmm.ui.panel.level_switch.LevelSwitchPanelUi
import strongdmm.ui.search.SearchResultPanelUi
import strongdmm.ui.tile_popup.TilePopupUi
import strongdmm.ui.panel.tool_select.ToolSelectPanelUi
import strongdmm.ui.panel.unknown_types.UnknownTypesPanelUi
import strongdmm.ui.vars.EditVarsDialogUi
import strongdmm.window.AppWindow
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

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
    private val closeMapDialogUi = CloseMapDialogUi()
    private val setMapSizeDialogUi = SetMapSizeDialogUi()
    private val unknownTypesPanelUi = UnknownTypesPanelUi()
    private val aboutPanelUi = AboutPanelUi()
    private val changelogPanelUi = ChangelogPanelUi()

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
    private val changelogController = ChangelogController()

    private val applicationCloseController = ApplicationCloseController()

    init {
        ensureHomeDirExists()
        ensureLogsDirExists()

        instanceLocatorPanelUi.postInit()
        mapHolderController.postInit()
        frameController.postInit()
        actionController.postInit()
        canvasController.postInit()
        recentFilesController.postInit()
        preferencesController.postInit()
        changelogController.postInit()
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
        closeMapDialogUi.process()
        setMapSizeDialogUi.process()
        unknownTypesPanelUi.process()
        aboutPanelUi.process()
        changelogPanelUi.process()

        // Controllers
        canvasController.process()
        shortcutController.process()

        // This should always be the last
        applicationCloseController.process()
    }

    private fun ensureHomeDirExists() = homeDir.toFile().mkdirs()
    private fun ensureLogsDirExists() = logsDir.toFile().mkdirs()

    companion object {
        const val TITLE: String = "StrongDMM"

        val homeDir: Path = Paths.get(System.getProperty("user.home")).resolve(".strongdmm")
        val logsDir: Path = homeDir.resolve("logs")

        @JvmStatic
        fun main(args: Array<String>) {
            try {
                StrongDMM(TITLE).start()
                exitProcess(0)
            } catch (e: Exception) {
                LoggerFactory.getLogger(this::class.java).error("Unhandled exception", e)
            }
        }
    }
}
