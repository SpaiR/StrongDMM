package strongdmm.ui

import imgui.ImGui
import imgui.ImGui.separator
import imgui.ImGui.text
import imgui.dsl.mainMenuBar
import imgui.dsl.menu
import imgui.dsl.menuBar
import imgui.dsl.menuItem
import strongdmm.byond.dme.Dme
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.Message
import strongdmm.native.NfdUtil

class MenuBarUi : EventSender, EventConsumer {
    private var progressText: String? = null
    private var isEnvironmentOpen: Boolean = false

    init {
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
    }

    fun process() {
        mainMenuBar {
            menuBar {
                menu("File") {
                    menuItem("Open Environment..", enabled = progressText == null, block = ::openEnvironment)
                    separator()
                    menuItem("Open Map..", shortcut = "Ctrl+O", enabled = isEnvironmentOpen, block = ::openMap)
                }

                progressText?.let {
                    val count = (ImGui.time / 0.25).toInt() and 3
                    val bar = charArrayOf('|', '/', '-', '\\')
                    text("%s %s%s", bar[count], it, ".".repeat(count))
                }
            }
        }
    }

    private fun openEnvironment() {
        NfdUtil.selectFile("dme")?.let { path ->
            progressText = "Loading " + path.substringAfterLast("\\")
            sendEvent<String, Boolean>(Event.ENVIRONMENT_OPEN, path) {
                progressText = null
                isEnvironmentOpen = it
            }
        }
    }

    private fun openMap() {
        sendEvent<Dme>(Event.ENVIRONMENT_FETCH) { environment ->
            NfdUtil.selectFile("dmm", environment.rootPath)?.let { path ->
                sendEvent(Event.MAP_OPEN, path)
            }
        }
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        isEnvironmentOpen = false
    }
}
