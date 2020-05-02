package strongdmm.ui.menu_bar

import imgui.ImBool
import org.lwjgl.glfw.GLFW
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.event.EventHandler
import strongdmm.event.type.controller.*
import strongdmm.event.type.ui.*
import strongdmm.util.NfdUtil
import strongdmm.window.AppWindow
import java.io.File

class ViewController(
    private val state: State
) : EventHandler {
    fun doOpenEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            sendEvent(TriggerEnvironmentController.OpenEnvironment(file))
        }
    }

    fun doOpenRecentEnvironment(environmentPath: String) {
        sendEvent(TriggerEnvironmentController.OpenEnvironment(File(environmentPath)))
    }

    fun doClearRecentEnvironments() {
        sendEvent(TriggerRecentFilesController.ClearRecentEnvironments())
    }

    fun doNewMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                sendEvent(TriggerMapHolderController.CreateNewMap(file))
            }
        })
    }

    fun doOpenMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment { environment ->
            NfdUtil.selectFile("dmm", environment.absRootDirPath)?.let { path ->
                sendEvent(TriggerMapHolderController.OpenMap(path))
            }
        })
    }

    fun doOpenAvailableMap() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerAvailableMapsDialogUi.Open())
        }
    }

    fun doOpenRecentMap(mapPath: String) {
        sendEvent(TriggerMapHolderController.OpenMap(File(mapPath)))
    }

    fun doClearRecentMaps() {
        sendEvent(TriggerRecentFilesController.ClearRecentMaps())
    }

    fun doCloseMap() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderController.CloseSelectedMap())
        }
    }

    fun doCloseAllMaps() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderController.CloseAllMaps())
        }
    }

    fun doSave() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderController.SaveSelectedMap())
        }
    }

    fun doSaveAll() {
        if (state.isEnvironmentOpened) {
            sendEvent(TriggerMapHolderController.SaveAllMaps())
        }
    }

    fun doSaveAs() {
        sendEvent(TriggerEnvironmentController.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                sendEvent(TriggerMapHolderController.SaveSelectedMapToFile(file))
            }
        })
    }

    fun doExit() {
        GLFW.glfwSetWindowShouldClose(AppWindow.windowPtr, true)
    }

    fun doUndo() {
        sendEvent(TriggerActionController.UndoAction())
    }

    fun doRedo() {
        sendEvent(TriggerActionController.RedoAction())
    }

    fun doCut() {
        sendEvent(TriggerClipboardController.Cut())
    }

    fun doCopy() {
        sendEvent(TriggerClipboardController.Copy())
    }

    fun doPaste() {
        sendEvent(TriggerClipboardController.Paste())
    }

    fun doDelete() {
        sendEvent(TriggerMapModifierController.DeleteTileItemsInSelectedArea())
    }

    fun doDeselectAll() {
        sendEvent(TriggerToolsController.ResetTool())
    }

    fun doSetMapSize() {
        sendEvent(TriggerSetMapSizeDialogUi.Open())
    }

    fun doFindInstance() {
        state.providedShowInstanceLocator.set(!state.providedShowInstanceLocator.get())
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
            sendEvent(TriggerLayersFilterController.ShowLayersByType(layerType))
        } else {
            sendEvent(TriggerLayersFilterController.HideLayersByType(layerType))
        }
    }
}
