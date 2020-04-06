package strongdmm.ui

import imgui.ImBool
import imgui.ImGui
import imgui.ImGui.separator
import imgui.ImGui.text
import org.lwjgl.glfw.GLFW
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.MapPath
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.shortcut.Shortcut
import strongdmm.controller.shortcut.ShortcutHandler
import strongdmm.event.DmeItemType
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.event.type.EventGlobalProvider
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.EventAvailableMapsDialogUi
import strongdmm.event.type.ui.EventLayersFilterPanelUi
import strongdmm.event.type.ui.EventPreferencesPanelUi
import strongdmm.event.type.ui.EventSetMapSizeDialogUi
import strongdmm.util.NfdUtil
import strongdmm.util.imgui.mainMenuBar
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem
import strongdmm.window.AppWindow
import java.io.File

class MenuBarUi : EventSender, EventConsumer, ShortcutHandler() {
    private var progressText: String? = null
    private var isEnvironmentOpened: Boolean = false

    private var isUndoEnabled: Boolean = false
    private var isRedoEnabled: Boolean = false

    private val isAreaLayerActive: ImBool = ImBool(true)
    private val isTurfLayerActive: ImBool = ImBool(true)
    private val isObjLayerActive: ImBool = ImBool(true)
    private val isMobLayerActive: ImBool = ImBool(true)

    private lateinit var providedShowInstanceLocator: ImBool
    private lateinit var providedFrameAreas: ImBool
    private lateinit var providedRecentEnvironments: List<String>
    private lateinit var providedRecentMaps: List<MapPath>

    init {
        consumeEvent(EventGlobal.EnvironmentLoading::class.java, ::handleEnvironmentLoading)
        consumeEvent(EventGlobal.EnvironmentLoaded::class.java, ::handleEnvironmentLoaded)
        consumeEvent(EventGlobal.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(EventGlobal.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(EventGlobal.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        consumeEvent(EventGlobal.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)

        consumeEvent(EventGlobalProvider.InstanceLocatorPanelUiOpen::class.java, ::handleProviderInstanceLocatorPanelUiOpen)
        consumeEvent(EventGlobalProvider.CanvasControllerFrameAreas::class.java, ::handleProviderCanvasControllerFrameAreas)
        consumeEvent(EventGlobalProvider.RecentFilesControllerRecentEnvironments::class.java, ::handleProviderRecentFilesControllerRecentEnvironments)
        consumeEvent(EventGlobalProvider.RecentFilesControllerRecentMaps::class.java, ::handleProviderRecentFilesControllerRecentMaps)

        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_O, action = ::doOpenMap)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_O, action = ::doOpenAvailableMap)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_W, action = ::doCloseMap)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_W, action = ::doCloseAllMaps)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_S, action = ::doSave)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Q, action = ::doExit)

        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_Z, action = ::doUndo)
        addShortcut(Shortcut.CONTROL_PAIR, Shortcut.SHIFT_PAIR, GLFW.GLFW_KEY_Z, action = ::doRedo)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_X, action = ::doCut)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_C, action = ::doCopy)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_V, action = ::doPaste)
        addShortcut(GLFW.GLFW_KEY_DELETE, action = ::doDelete)
        addShortcut(GLFW.GLFW_KEY_ESCAPE, action = ::doDeselectAll)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_F, action = ::doFindInstance)

        // "Manual" methods since toggle through the buttons switches ImBool status vars automatically.
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_1, action = ::toggleAreaLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_2, action = ::toggleTurfLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_3, action = ::toggleObjLayerManual)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_4, action = ::toggleMobLayerManual)
    }

    fun process() {
        mainMenuBar {
            menu("File") {
                menuItem("Open Environment...", enabled = progressText == null, block = ::doOpenEnvironment)
                menu("Recent Environments", enabled = progressText == null) {
                    showRecentEnvironments()
                }
                separator()
                menuItem("Open Map...", shortcut = "Ctrl+O", enabled = isEnvironmentOpened, block = ::doOpenMap)
                menuItem("Open Available Map", shortcut = "Ctrl+Shift+O", enabled = isEnvironmentOpened, block = ::doOpenAvailableMap)
                menu("Recent Maps", enabled = isEnvironmentOpened) { showRecentMaps() }
                separator()
                menuItem("Close Map", shortcut = "Ctrl+W", enabled = isEnvironmentOpened, block = ::doCloseMap)
                menuItem("Close All Maps", shortcut = "Ctrl+Shift+W", enabled = isEnvironmentOpened, block = ::doCloseAllMaps)
                separator()
                menuItem("Save", shortcut = "Ctrl+S", enabled = isEnvironmentOpened, block = ::doSave)
                separator()
                menuItem("Exit", shortcut = "Ctrl+Q", block = ::doExit)
            }

            menu("Edit") {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = isUndoEnabled, block = ::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = isRedoEnabled, block = ::doRedo)
                separator()
                menuItem("Cut", shortcut = "Ctrl+X", enabled = isEnvironmentOpened, block = ::doCut)
                menuItem("Copy", shortcut = "Ctrl+C", enabled = isEnvironmentOpened, block = ::doCopy)
                menuItem("Paste", shortcut = "Ctrl+V", enabled = isEnvironmentOpened, block = ::doPaste)
                menuItem("Delete", shortcut = "Delete", enabled = isEnvironmentOpened, block = ::doDelete)
                menuItem("Deselect All", shortcut = "Esc", block = ::doDeselectAll)
                separator()
                menuItem("Set Map Size...", enabled = isEnvironmentOpened, block = ::doSetMapSize)
                menuItem("Find Instance...", shortcut = "Ctrl+F", block = ::doFindInstance)
            }

            menu("Options") {
                menuItem("Layers Filter", enabled = isEnvironmentOpened, block = ::doOpenLayersFilter)
                menuItem("Toggle Area", shortcut = "Ctrl+1", enabled = isEnvironmentOpened, selected = isAreaLayerActive, block = ::toggleAreaLayer)
                menuItem("Toggle Turf", shortcut = "Ctrl+2", enabled = isEnvironmentOpened, selected = isTurfLayerActive, block = ::toggleTurfLayer)
                menuItem("Toggle Object", shortcut = "Ctrl+3", enabled = isEnvironmentOpened, selected = isObjLayerActive, block = ::toggleObjLayer)
                menuItem("Toggle Mob", shortcut = "Ctrl+4", enabled = isEnvironmentOpened, selected = isMobLayerActive, block = ::toggleMobLayer)
                separator()
                menuItem("Frame Areas", selected = providedFrameAreas, block = {})
                menuItem("Preferences...", block = ::doOpenPreferences)
            }

            menu("Window") {
                menuItem("Reset Windows", block = ::doResetWindows)
            }

            progressText?.let {
                val count = (ImGui.getTime() / 0.25).toInt() and 3
                val bar = charArrayOf('|', '/', '-', '\\')
                text("${bar[count]} $it${".".repeat(count)}")
            }
        }
    }

    private fun showRecentEnvironments() {
        if (providedRecentEnvironments.isEmpty()) {
            return
        }

        providedRecentEnvironments.toTypedArray().forEach { recentEnvironmentPath ->
            menuItem(recentEnvironmentPath) {
                sendEvent(EventEnvironmentController.OpenEnvironment(File(recentEnvironmentPath)))
            }
        }
        separator()
        menuItem("Clear Recent Environments") {
            sendEvent(EventRecentFilesController.ClearRecentEnvironments())
        }
    }

    private fun showRecentMaps() {
        if (providedRecentMaps.isEmpty()) {
            return
        }

        providedRecentMaps.toTypedArray().forEach { (readable, absolute) ->
            menuItem(readable) {
                sendEvent(EventMapHolderController.OpenMap(File(absolute)))
            }
        }
        separator()
        menuItem("Clear Recent Maps") {
            sendEvent(EventRecentFilesController.ClearRecentMaps())
        }
    }

    private fun doOpenEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            sendEvent(EventEnvironmentController.OpenEnvironment(file))
        }
    }

    private fun doOpenMap() {
        if (!isEnvironmentOpened) {
            return
        }

        sendEvent(EventEnvironmentController.FetchOpenedEnvironment { environment ->
            NfdUtil.selectFile("dmm", environment.absRootDirPath)?.let { path ->
                sendEvent(EventMapHolderController.OpenMap(path))
            }
        })
    }

    private fun doOpenAvailableMap() {
        if (isEnvironmentOpened) {
            sendEvent(EventAvailableMapsDialogUi.Open())
        }
    }

    private fun doCloseMap() {
        if (isEnvironmentOpened) {
            sendEvent(EventMapHolderController.CloseSelectedMap())
        }
    }

    private fun doCloseAllMaps() {
        if (isEnvironmentOpened) {
            sendEvent(EventMapHolderController.CloseAllMaps())
        }
    }

    private fun doSave() {
        if (isEnvironmentOpened) {
            sendEvent(EventMapHolderController.SaveSelectedMap())
        }
    }

    private fun doExit() {
        GLFW.glfwSetWindowShouldClose(AppWindow.window, true)
    }

    private fun doUndo() {
        sendEvent(EventActionController.UndoAction())
    }

    private fun doRedo() {
        sendEvent(EventActionController.RedoAction())
    }

    private fun doCut() {
        sendEvent(EventClipboardController.Cut())
    }

    private fun doCopy() {
        sendEvent(EventClipboardController.Copy())
    }

    private fun doPaste() {
        sendEvent(EventClipboardController.Paste())
    }

    private fun doDelete() {
        sendEvent(EventMapModifierController.DeleteTileItemsInActiveArea())
    }

    private fun doDeselectAll() {
        sendEvent(EventToolsController.ResetTool())
    }

    private fun doSetMapSize() {
        sendEvent(EventSetMapSizeDialogUi.Open())
    }

    private fun doFindInstance() {
        providedShowInstanceLocator.set(!providedShowInstanceLocator.get())
    }

    private fun doOpenPreferences() {
        sendEvent(EventPreferencesPanelUi.Open())
    }

    private fun doOpenLayersFilter() {
        sendEvent(EventLayersFilterPanelUi.Open())
    }

    private fun toggleAreaLayer() {
        toggleLayer(isAreaLayerActive, TYPE_AREA)
    }

    private fun toggleAreaLayerManual() {
        if (isEnvironmentOpened) {
            isAreaLayerActive.set(!isAreaLayerActive.get())
            toggleLayer(isAreaLayerActive, TYPE_AREA)
        }
    }

    private fun toggleTurfLayer() {
        toggleLayer(isTurfLayerActive, TYPE_TURF)
    }

    private fun toggleTurfLayerManual() {
        if (isEnvironmentOpened) {
            isTurfLayerActive.set(!isTurfLayerActive.get())
            toggleLayer(isTurfLayerActive, TYPE_TURF)
        }
    }

    private fun toggleObjLayer() {
        toggleLayer(isObjLayerActive, TYPE_OBJ)
    }

    private fun toggleObjLayerManual() {
        if (isEnvironmentOpened) {
            isObjLayerActive.set(!isObjLayerActive.get())
            toggleLayer(isObjLayerActive, TYPE_OBJ)
        }
    }

    private fun toggleMobLayer() {
        toggleLayer(isMobLayerActive, TYPE_MOB)
    }

    private fun toggleMobLayerManual() {
        if (isEnvironmentOpened) {
            isMobLayerActive.set(!isMobLayerActive.get())
            toggleLayer(isMobLayerActive, TYPE_MOB)
        }
    }

    private fun toggleLayer(layerStatus: ImBool, layerType: String) {
        if (layerStatus.get()) {
            sendEvent(EventLayersFilterController.ShowLayersByType(layerType))
        } else {
            sendEvent(EventLayersFilterController.HideLayersByType(layerType))
        }
    }

    private fun doResetWindows() {
        AppWindow.resetWindows()
    }

    private fun handleEnvironmentLoading(event: Event<File, Unit>) {
        progressText = "Loading " + event.body.absolutePath.replace('\\', '/').substringAfterLast("/")
    }

    private fun handleEnvironmentLoaded() {
        progressText = null
    }

    private fun handleEnvironmentChanged() {
        isEnvironmentOpened = true
    }

    private fun handleEnvironmentReset() {
        isEnvironmentOpened = false
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        isUndoEnabled = event.body.hasUndoAction
        isRedoEnabled = event.body.hasRedoAction
    }

    private fun handleLayersFilterRefreshed(event: Event<Set<DmeItemType>, Unit>) {
        isAreaLayerActive.set(!event.body.contains(TYPE_AREA))
        isTurfLayerActive.set(!event.body.contains(TYPE_TURF))
        isObjLayerActive.set(!event.body.contains(TYPE_OBJ))
        isMobLayerActive.set(!event.body.contains(TYPE_MOB))
    }

    private fun handleProviderInstanceLocatorPanelUiOpen(event: Event<ImBool, Unit>) {
        providedShowInstanceLocator = event.body
    }

    private fun handleProviderCanvasControllerFrameAreas(event: Event<ImBool, Unit>) {
        providedFrameAreas = event.body
    }

    private fun handleProviderRecentFilesControllerRecentEnvironments(event: Event<List<String>, Unit>) {
        providedRecentEnvironments = event.body
    }

    private fun handleProviderRecentFilesControllerRecentMaps(event: Event<List<MapPath>, Unit>) {
        providedRecentMaps = event.body
    }
}
