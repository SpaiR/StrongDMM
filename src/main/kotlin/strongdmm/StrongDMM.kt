package strongdmm

import org.slf4j.LoggerFactory
import strongdmm.service.*
import strongdmm.service.action.ActionService
import strongdmm.service.canvas.CanvasService
import strongdmm.service.frame.FrameService
import strongdmm.service.map.MapHolderService
import strongdmm.service.map.MapModifierService
import strongdmm.service.preferences.PreferencesService
import strongdmm.service.recent.RecentFilesService
import strongdmm.service.shortcut.ShortcutService
import strongdmm.service.tool.ToolsService
import strongdmm.ui.dialog.available_maps.AvailableMapsDialogUi
import strongdmm.ui.dialog.close_map.CloseMapDialogUi
import strongdmm.ui.dialog.edit_vars.EditVarsDialogUi
import strongdmm.ui.dialog.set_map_size.SetMapSizeDialogUi
import strongdmm.ui.menu_bar.MenuBarUi
import strongdmm.ui.panel.about.AboutPanelUi
import strongdmm.ui.panel.changelog.ChangelogPanelUi
import strongdmm.ui.panel.coords.CoordsPanelUi
import strongdmm.ui.panel.environment_tree.EnvironmentTreePanelUi
import strongdmm.ui.panel.instance_locator.InstanceLocatorPanelUi
import strongdmm.ui.panel.layers_filter.LayersFilterPanelUi
import strongdmm.ui.panel.level_switch.LevelSwitchPanelUi
import strongdmm.ui.panel.objects.ObjectsPanelUi
import strongdmm.ui.panel.opened_maps.OpenedMapsPanelUi
import strongdmm.ui.panel.preferences.PreferencesPanelUi
import strongdmm.ui.panel.search_result.SearchResultPanelUi
import strongdmm.ui.panel.tool_select.ToolSelectPanelUi
import strongdmm.ui.panel.unknown_types.UnknownTypesPanelUi
import strongdmm.ui.panel.variables_preview.VariablesPreviewPanelUi
import strongdmm.ui.tile_popup.TilePopupUi
import strongdmm.window.AppWindow
import java.nio.file.Path
import java.nio.file.Paths
import kotlin.system.exitProcess

class StrongDMM(title: String) : AppWindow(title) {
    private val uiList: List<Ui> = listOf(
        MenuBarUi(),
        CoordsPanelUi(),
        OpenedMapsPanelUi(),
        AvailableMapsDialogUi(),
        TilePopupUi(),
        EditVarsDialogUi(),
        EnvironmentTreePanelUi(),
        ObjectsPanelUi(),
        VariablesPreviewPanelUi(),
        InstanceLocatorPanelUi(),
        SearchResultPanelUi(),
        LayersFilterPanelUi(),
        ToolSelectPanelUi(),
        LevelSwitchPanelUi(),
        PreferencesPanelUi(),
        CloseMapDialogUi(),
        SetMapSizeDialogUi(),
        UnknownTypesPanelUi(),
        AboutPanelUi(),
        ChangelogPanelUi()
    )

    private val windowTitleService = WindowTitleService()
    private val environmentController = EnvironmentService()
    private val mapHolderController = MapHolderService()
    private val mapModifierController = MapModifierService()
    private val canvasController = CanvasService()
    private val frameController = FrameService()
    private val actionController = ActionService()
    private val instanceController = InstanceService()
    private val layersFilterController = LayersFilterService()
    private val toolsController = ToolsService()
    private val shortcutController = ShortcutService()
    private val clipboardController = ClipboardService()
    private val tileItemController = TileItemService()
    private val recentFilesController = RecentFilesService()
    private val preferencesController = PreferencesService()
    private val changelogController = ChangelogService()

    private val applicationCloseController = ApplicationCloseService()

    private val processableList: List<Processable> = uiList.filterIsInstance(Processable::class.java)

    init {
        ensureHomeDirExists()
        ensureLogsDirExists()

        uiList.filterIsInstance(PostInitialize::class.java).forEach(PostInitialize::postInit)

        mapHolderController.postInit()
        frameController.postInit()
        actionController.postInit()
        canvasController.postInit()
        recentFilesController.postInit()
        preferencesController.postInit()
        changelogController.postInit()
    }

    override fun appLoop() {
        processableList.forEach(Processable::process)

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
