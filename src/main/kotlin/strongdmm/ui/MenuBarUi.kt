package strongdmm.ui

import imgui.ImBool
import imgui.ImGui
import imgui.ImGui.separator
import imgui.ImGui.text
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.controller.action.ActionStatus
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.NfdUtil
import strongdmm.util.imgui.mainMenuBar
import strongdmm.util.imgui.menu
import strongdmm.util.imgui.menuItem

class MenuBarUi : EventSender, EventConsumer {
    private var progressText: String? = null
    private var isEnvironmentOpened: Boolean = false

    private var isUndoEnabled: Boolean = false
    private var isRedoEnabled: Boolean = false

    private val isAreaLayerActive: ImBool = ImBool(true)
    private val isTurfLayerActive: ImBool = ImBool(true)
    private val isObjLayerActive: ImBool = ImBool(true)
    private val isMobLayerActive: ImBool = ImBool(true)

    init {
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.ActionStatusChanged::class.java, ::handleActionStatusChanged)
    }

    fun process() {
        mainMenuBar {
            menu("File") {
                menuItem("Open Environment...", enabled = progressText == null, block = ::openEnvironment)
                separator()
                menuItem("Open Map...", shortcut = "Ctrl+O", enabled = isEnvironmentOpened, block = ::openMap)
                menuItem("Open Available Map", enabled = isEnvironmentOpened, block = ::openAvailableMap)
                separator()
                menuItem("Save", shortcut = "Ctrl+S", enabled = isEnvironmentOpened, block = ::save)
            }

            menu("Edit") {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = isUndoEnabled, block = ::doUndo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = isRedoEnabled, block = ::doRedo)
            }

            menu("Layers") {
                menuItem("Layers Filter", enabled = isEnvironmentOpened, block = ::openLayersFilter)
                menuItem("Area", shortcut = "Ctrl+1", enabled = isEnvironmentOpened, selected = isAreaLayerActive, block = ::toggleAreaLayer)
                menuItem("Turf", shortcut = "Ctrl+2", enabled = isEnvironmentOpened, selected = isTurfLayerActive, block = ::toggleTurfLayer)
                menuItem("Object", shortcut = "Ctrl+3", enabled = isEnvironmentOpened, selected = isObjLayerActive, block = ::toggleObjLayer)
                menuItem("Mob", shortcut = "Ctrl+4", enabled = isEnvironmentOpened, selected = isMobLayerActive, block = ::toggleMobLayer)
            }

            progressText?.let {
                val count = (ImGui.getTime() / 0.25).toInt() and 3
                val bar = charArrayOf('|', '/', '-', '\\')
                text("${bar[count]} $it${".".repeat(count)}")
            }
        }
    }

    private fun openEnvironment() {
        NfdUtil.selectFile("dme")?.let { file ->
            progressText = "Loading " + file.absolutePath.replace('\\', '/').substringAfterLast("/")
            sendEvent(Event.EnvironmentController.Open(file) {
                progressText = null
                isEnvironmentOpened = it
            })
        }
    }

    private fun openMap() {
        sendEvent(Event.EnvironmentController.Fetch { environment ->
            NfdUtil.selectFile("dmm", environment.rootPath)?.let { path ->
                sendEvent(Event.MapHolderController.Open(path))
            }
        })
    }

    private fun openAvailableMap() {
        sendEvent(Event.AvailableMapsDialogUi.Open())
    }

    private fun save() {
        sendEvent(Event.MapHolderController.Save())
    }

    private fun doUndo() {
        sendEvent(Event.ActionController.UndoAction())
    }

    private fun doRedo() {
        sendEvent(Event.ActionController.RedoAction())
    }

    private fun openLayersFilter() {
        sendEvent(Event.LayersFilterPanelUi.Open())
    }

    private fun toggleAreaLayer() {
        toggleLayer(isAreaLayerActive, TYPE_AREA)
    }

    private fun toggleTurfLayer() {
        toggleLayer(isTurfLayerActive, TYPE_TURF)
    }

    private fun toggleObjLayer() {
        toggleLayer(isObjLayerActive, TYPE_OBJ)
    }

    private fun toggleMobLayer() {
        toggleLayer(isMobLayerActive, TYPE_MOB)
    }

    private fun toggleLayer(layerStatus: ImBool, layerType: String) {
        if (layerStatus.get()) {
            sendEvent(Event.LayersFilterController.ShowByType(layerType))
        } else {
            sendEvent(Event.LayersFilterController.HideByType(layerType))
        }
    }

    private fun handleResetEnvironment() {
        isEnvironmentOpened = false
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        isUndoEnabled = event.body.hasUndoAction
        isRedoEnabled = event.body.hasRedoAction
    }
}
