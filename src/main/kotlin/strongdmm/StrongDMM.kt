package strongdmm

import org.slf4j.LoggerFactory
import strongdmm.application.PostInitialize
import strongdmm.application.Processable
import strongdmm.application.Service
import strongdmm.application.Ui
import strongdmm.service.*
import strongdmm.service.action.ActionService
import strongdmm.service.canvas.CanvasService
import strongdmm.service.changelog.ChangelogService
import strongdmm.service.frame.FrameService
import strongdmm.service.map.MapHolderService
import strongdmm.service.map.MapModifierService
import strongdmm.service.map.MapPreprocessService
import strongdmm.service.pinnedvariables.PinnedVariablesService
import strongdmm.service.preferences.PreferencesService
import strongdmm.service.recent.RecentFilesService
import strongdmm.service.screenshot.ScreenshotService
import strongdmm.service.settings.SettingsService
import strongdmm.service.shortcut.ShortcutService
import strongdmm.service.tool.ToolsService
import strongdmm.ui.dialog.available_maps.AvailableMapsDialogUi
import strongdmm.ui.dialog.confirmation.ConfirmationDialogUi
import strongdmm.ui.dialog.edit_vars.EditVarsDialogUi
import strongdmm.ui.dialog.set_map_size.SetMapSizeDialogUi
import strongdmm.ui.dialog.unknown_types.UnknownTypesDialogUi
import strongdmm.ui.menubar.MenuBarUi
import strongdmm.ui.panel.about.AboutPanelUi
import strongdmm.ui.panel.changelog.ChangelogPanelUi
import strongdmm.ui.panel.coords.CoordsPanelUi
import strongdmm.ui.panel.environmenttree.EnvironmentTreePanelUi
import strongdmm.ui.panel.instancelocator.InstanceLocatorPanelUi
import strongdmm.ui.panel.layersfilter.LayersFilterPanelUi
import strongdmm.ui.panel.levelswitch.LevelSwitchPanelUi
import strongdmm.ui.panel.notification.NotificationPanelUi
import strongdmm.ui.panel.objects.ObjectsPanelUi
import strongdmm.ui.panel.openedmaps.OpenedMapsPanelUi
import strongdmm.ui.panel.preferences.PreferencesPanelUi
import strongdmm.ui.panel.progress.ProgressPanelUi
import strongdmm.ui.panel.screenshot.ScreenshotPanelUi
import strongdmm.ui.panel.searchresult.SearchResultPanelUi
import strongdmm.ui.panel.toolselect.ToolSelectPanelUi
import strongdmm.ui.panel.variablespreview.VariablesPreviewPanelUi
import strongdmm.ui.tilepopup.TilePopupUi
import strongdmm.application.window.AppWindow
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
        SetMapSizeDialogUi(),
        AboutPanelUi(),
        ChangelogPanelUi(),
        ScreenshotPanelUi(),
        ConfirmationDialogUi(),
        NotificationPanelUi(),
        ProgressPanelUi(),
        UnknownTypesDialogUi()
    )

    private val serviceList: List<Service> = listOf(
        WindowTitleService(),
        EnvironmentService(),
        MapHolderService(),
        MapModifierService(),
        MapPreprocessService(),
        CanvasService(),
        FrameService(),
        ActionService(),
        InstanceService(),
        LayersFilterService(),
        ToolsService(),
        ShortcutService(),
        ClipboardService(),
        TileItemService(),
        RecentFilesService(),
        PreferencesService(),
        ChangelogService(),
        PinnedVariablesService(),
        ScreenshotService(),
        SettingsService(),
        ApplicationCloseService()
    )

    private val processableList: List<Processable> = uiList.filterIsInstance(Processable::class.java) + serviceList.filterIsInstance(Processable::class.java)

    init {
        ensureHomeDirExists()
        ensureLogsDirExists()

        check(serviceList.last() is ApplicationCloseService) {
            "ApplicationCloseService SHOULD be always the LAST in the services list"
        }

        uiList.filterIsInstance(PostInitialize::class.java).forEach(PostInitialize::postInit)
        serviceList.filterIsInstance(PostInitialize::class.java).forEach(PostInitialize::postInit)
    }

    override fun applicationLoop() {
        processableList.forEach(Processable::process)
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
