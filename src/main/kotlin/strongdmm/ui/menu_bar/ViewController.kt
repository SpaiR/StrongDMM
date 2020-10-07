package strongdmm.ui.menu_bar

import imgui.type.ImBoolean
import org.lwjgl.glfw.GLFW
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.event.EventBus
import strongdmm.event.type.service.*
import strongdmm.event.type.ui.*
import strongdmm.util.NfdUtil
import strongdmm.window.AppWindow
import strongdmm.window.Window
import java.io.File

class ViewController(
    private val state: State
) {
    fun doOpenEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            EventBus.post(TriggerEnvironmentService.OpenEnvironment(file))
        }
    }

    fun doOpenRecentEnvironment(environmentPath: String) {
        EventBus.post(TriggerEnvironmentService.OpenEnvironment(File(environmentPath)))
    }

    fun doClearRecentEnvironments() {
        EventBus.post(TriggerRecentFilesService.ClearRecentEnvironments())
    }

    fun doNewMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                EventBus.post(TriggerMapHolderService.CreateNewMap(file))
            }
        })
    }

    fun doOpenMap() {
        if (!state.isEnvironmentOpened) {
            return
        }

        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment { environment ->
            NfdUtil.selectFile("dmm", environment.absRootDirPath)?.let { path ->
                EventBus.post(TriggerMapHolderService.OpenMap(path))
            }
        })
    }

    fun doOpenAvailableMap() {
        if (state.isEnvironmentOpened) {
            EventBus.post(TriggerAvailableMapsDialogUi.Open())
        }
    }

    fun doOpenRecentMap(mapPath: String) {
        EventBus.post(TriggerMapHolderService.OpenMap(File(mapPath)))
    }

    fun doClearRecentMaps() {
        EventBus.post(TriggerRecentFilesService.ClearRecentMaps())
    }

    fun doCloseMap() {
        if (state.isEnvironmentOpened) {
            EventBus.post(TriggerMapHolderService.CloseSelectedMap())
        }
    }

    fun doCloseAllMaps() {
        if (state.isEnvironmentOpened) {
            EventBus.post(TriggerMapHolderService.CloseAllMaps())
        }
    }

    fun doSave() {
        if (state.isEnvironmentOpened) {
            EventBus.post(TriggerMapHolderService.SaveSelectedMap())
        }
    }

    fun doSaveAll() {
        if (state.isEnvironmentOpened) {
            EventBus.post(TriggerMapHolderService.SaveAllMaps())
        }
    }

    fun doSaveAs() {
        EventBus.post(TriggerEnvironmentService.FetchOpenedEnvironment {
            NfdUtil.saveFile("dmm", it.absRootDirPath)?.let { file ->
                EventBus.post(TriggerMapHolderService.SaveSelectedMapToFile(file))
            }
        })
    }

    fun doExit() {
        GLFW.glfwSetWindowShouldClose(Window.ptr, true)
    }

    fun doUndo() {
        EventBus.post(TriggerActionService.UndoAction())
    }

    fun doRedo() {
        EventBus.post(TriggerActionService.RedoAction())
    }

    fun doCut() {
        EventBus.post(TriggerClipboardService.Cut())
    }

    fun doCopy() {
        EventBus.post(TriggerClipboardService.Copy())
    }

    fun doPaste() {
        EventBus.post(TriggerClipboardService.Paste())
    }

    fun doDelete() {
        EventBus.post(TriggerMapModifierService.DeleteTileItemsInSelectedArea())
    }

    fun doDeselectAll() {
        EventBus.post(TriggerToolsService.ResetTool())
    }

    fun doSetMapSize() {
        EventBus.post(TriggerSetMapSizeDialogUi.Open())
    }

    fun doFindInstance() {
        state.providedShowInstanceLocator.set(!state.providedShowInstanceLocator.get())
    }

    fun doScreenshot() {
        EventBus.post(TriggerScreenshotPanelUi.Open())
    }

    fun doOpenPreferences() {
        EventBus.post(TriggerPreferencesPanelUi.Open())
    }

    fun doOpenLayersFilter() {
        EventBus.post(TriggerLayersFilterPanelUi.Open())
    }

    fun doResetWindows() {
        AppWindow.resetWindows()
    }

    fun doFullscreen() {
        AppWindow.toggleFullscreen()
    }

    fun doChangelog() {
        EventBus.post(TriggerChangelogPanelUi.Open())
    }

    fun doAbout() {
        EventBus.post(TriggerAboutPanelUi.Open())
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

    fun toggleLayer(layerStatus: ImBoolean, layerType: String) {
        if (layerStatus.get()) {
            EventBus.post(TriggerLayersFilterService.ShowLayersByTypeExact(layerType))
        } else {
            EventBus.post(TriggerLayersFilterService.HideLayersByTypeExact(layerType))
        }
    }
}
