package strongdmm.ui

import imgui.ImGui
import imgui.ImGui.separator
import imgui.ImGui.text
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
    private var isEnvironmentOpen: Boolean = false

    private var isUndoEnabled: Boolean = false
    private var isRedoEnabled: Boolean = false

    init {
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.ActionStatusChanged::class.java, ::handleActionStatusChanged)
    }

    fun process() {
        mainMenuBar {
            menu("File") {
                menuItem("Open Environment...", enabled = progressText == null, block = ::openEnvironment)
                separator()
                menuItem("Open Map...", shortcut = "Ctrl+O", enabled = isEnvironmentOpen, block = ::openMap)
                menuItem("Open Available Map", enabled = isEnvironmentOpen, block = ::openAvailableMap)
                separator()
                menuItem("Save", shortcut = "Ctrl+S", enabled = isEnvironmentOpen, block = ::save)
            }

            menu("Edit") {
                menuItem("Undo", shortcut = "Ctrl+Z", enabled = isUndoEnabled, block = ::undo)
                menuItem("Redo", shortcut = "Ctrl+Shift+Z", enabled = isRedoEnabled, block = ::redo)
            }

            progressText?.let {
                val count = (ImGui.getTime() / 0.25).toInt() and 3
                val bar = charArrayOf('|', '/', '-', '\\')
                text("${bar[count]} $it${".".repeat(count)}")
            }
        }
    }

    private fun openEnvironment() {
        NfdUtil.selectFile("dme")?.let { path ->
            progressText = "Loading " + path.value.replace('\\', '/').substringAfterLast("/")
            sendEvent(Event.EnvironmentController.Open(path) {
                progressText = null
                isEnvironmentOpen = it.isOpen()
            })
        }
    }

    private fun openMap() {
        sendEvent(Event.EnvironmentController.Fetch { environment ->
            NfdUtil.selectFile("dmm", environment.rootPath)?.let { path ->
                sendEvent(Event.MapController.Open(path))
            }
        })
    }

    private fun openAvailableMap() {
        sendEvent(Event.AvailableMapsDialogUi.Open())
    }

    private fun save() {
        sendEvent(Event.MapController.Save())
    }

    private fun undo() {
        sendEvent(Event.ActionController.UndoAction())
    }

    private fun redo() {
        sendEvent(Event.ActionController.RedoAction())
    }

    private fun handleResetEnvironment() {
        isEnvironmentOpen = false
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        isUndoEnabled = event.body.hasUndoAction
        isRedoEnabled = event.body.hasRedoAction
    }
}
