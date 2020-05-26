package strongdmm.ui.menu_bar

import imgui.ImBool
import org.lwjgl.glfw.GLFW
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.event.EventHandler
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.*
import strongdmm.util.NfdUtil
import strongdmm.window.AppWindow
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    fun doOpenEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            sendEvent(TriggerEnvironmentService.OpenEnvironment(file))
        }
    }

    fun doOpenRecentEnvironment(environmentPath: String) {
        sendEvent(TriggerEnvironmentService.OpenEnvironment(File(environmentPath)))
    }

    fun doClearRecentEnvironments() {
        sendEvent(TriggerRecentFilesService.ClearRecentEnvironments())
    }

    fun doNewMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                sendEvent(TriggerMapHolderService.CreateNewMap(file))
            }
        })
    }

    fun doOpenMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
            NfdUtil.selectFile("dmm", environment.absRootDirPath)?.let { path ->
                sendEvent(TriggerMapHolderService.OpenMap(path))
            }
        })
    }

    fun doOpenAvailableMap() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerAvailableMapsDialogUi.Open())
        }
    }

    fun doOpenRecentMap(mapPath: String) {
        sendEvent(TriggerMapHolderService.OpenMap(File(mapPath)))
    }

    fun doClearRecentMaps() {
        sendEvent(TriggerRecentFilesService.ClearRecentMaps())
    }

    fun doCloseMap() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderService.CloseSelectedMap())
        }
    }

    fun doCloseAllMaps() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderService.CloseAllMaps())
        }
    }

    fun doSave() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderService.SaveSelectedMap())
        }
    }

    fun doSaveAll() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderService.SaveAllMaps())
        }
    }

    fun doSaveAs() {
        sendEvent(TriggerEnvironmentService.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                sendEvent(TriggerMapHolderService.SaveSelectedMapToFile(file))
            }
        })
    }

    fun doExit() {
        GLFW.glfwSetWindowShouldClose(AppWindow.windowPtr, true)
    }

    fun doUndo() {
        sendEvent(TriggerActionService.UndoAction())
    }

    fun doRedo() {
        sendEvent(TriggerActionService.RedoAction())
    }

    fun doCut() {
        sendEvent(TriggerClipboardService.Cut())
    }

    fun doCopy() {
        sendEvent(TriggerClipboardService.Copy())
    }

    fun doPaste() {
        sendEvent(TriggerClipboardService.Paste())
    }

    fun doDelete() {
        sendEvent(TriggerMapModifierService.DeleteTileItemsInSelectedArea())
    }

    fun doDeselectAll() {
        sendEvent(TriggerToolsService.ResetTool())
    }

    fun doSetMapSize() {
        sendEvent(TriggerSetMapSizeDialogUi.Open())
    }

    fun doFindInstance() {
        state.providedShowInstanceLocator.set(!state.providedShowInstanceLocator.get())
    }

    fun doScreenshot() {
        sendEvent(TriggerScreenshotPanelUi.Open())
    }

    fun doOpenPreferences() {
        sendEvent(TriggerPreferencesPanelUi.Open())
    }

    fun doOpenLayersFilter() {
        sendEvent(TriggerLayersFilterPanelUi.Open())
    }

    fun doResetWindows() {
        AppWindow.resetWindows()
    }

    fun doFullscreen() {
        AppWindow.toggleFullscreen()
    }

    fun doChangelog() {
        sendEvent(TriggerChangelogPanelUi.Open())
    }

    fun doAbout() {
        sendEvent(TriggerAboutPanelUi.Open())
    }

    fun toggleAreaLayer() {
        toggleLayer(state.isAreaLayerActive, TYPE_AREA)
    }

    fun toggleAreaLayerManual() {
        if (state.isEnvironmentOpened) {
            state.isAreaLayerActive.set(!state.isAreaLayerActive.get())
            toggleLayer(state.isAreaLayerActive, TYPE_AREA)
        }
    }

    fun toggleTurfLayer() {
        toggleLayer(state.isTurfLayerActive, TYPE_TURF)
    }

    fun toggleTurfLayerManual() {
        if (state.isEnvironmentOpened) {
            state.isTurfLayerActive.set(!state.isTurfLayerActive.get())
            toggleLayer(state.isTurfLayerActive, TYPE_TURF)
        }
    }

    fun toggleObjLayer() {
        toggleLayer(state.isObjLayerActive, TYPE_OBJ)
    }

    fun toggleObjLayerManual() {
        if (state.isEnvironmentOpened) {
            state.isObjLayerActive.set(!state.isObjLayerActive.get())
            toggleLayer(state.isObjLayerActive, TYPE_OBJ)
        }
    }

    fun toggleMobLayer() {
        toggleLayer(state.isMobLayerActive, TYPE_MOB)
    }

    fun toggleMobLayerManual() {
        if (state.isEnvironmentOpened) {
            state.isMobLayerActive.set(!state.isMobLayerActive.get())
            toggleLayer(state.isMobLayerActive, TYPE_MOB)
        }
    }

    fun toggleLayer(layerStatus: ImBool, layerType: String) {
        if (layerStatus.get()) {
            sendEvent(TriggerLayersFilterService.ShowLayersByType(layerType))
        } else {
            sendEvent(TriggerLayersFilterService.HideLayersByType(layerType))
        }
    }
}
