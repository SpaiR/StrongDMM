package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.controller.shortcut.Shortcut
import strongdmm.controller.shortcut.ShortcutHandler
import strongdmm.controller.tool.ToolType
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.EventGlobal
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window

class ToolSelectPanelUi : EventConsumer, EventSender, ShortcutHandler() {
    private val tools: Array<ToolType> = ToolType.values()
    private var activeTool: ToolType = ToolType.TILE

    init {
        consumeEvent(EventGlobal.ActiveToolChanged::class.java, ::handleActiveToolChanged)
        consumeEvent(EventGlobal.ShortcutTriggered::class.java, ::handleShortcutTriggered)

        addShortcut(Shortcut.ALT_PAIR, GLFW.GLFW_KEY_1) { selectTool(tools[0]) }
        addShortcut(Shortcut.ALT_PAIR, GLFW.GLFW_KEY_2) { selectTool(tools[1]) }
        addShortcut(Shortcut.ALT_PAIR, GLFW.GLFW_KEY_3) { selectTool(tools[2]) }
    }

    fun process() {
        setNextWindowPos(350f, 30f, ImGuiCond.Once)
        setNextWindowSize(80f, 35f, ImGuiCond.Once)

        window("Tool", ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize) {
            tools.forEach { tool ->
                val isToolActive = tool == activeTool

                if (isToolActive) {
                    pushStyleColor(ImGuiCol.Button, 0f, .5f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonHovered, 0f, .8f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonActive, 0f, .5f, 0f, 1f)
                }

                button(tool.toolName) {
                    sendEvent(Event.ToolsController.Switch(tool))
                }

                if (isItemHovered()) {
                    setTooltip(tool.toolDesc)
                }

                if (isToolActive) {
                    popStyleColor(3)
                }

                sameLine()
            }
        }
    }

    private fun selectTool(tool: ToolType) {
        sendEvent(Event.ToolsController.Switch(tool))
    }

    private fun handleActiveToolChanged(event: Event<ToolType, Unit>) {
        activeTool = event.body
    }

    private fun handleShortcutTriggered(event: Event<Shortcut, Unit>) {
        handleShortcut(event.body)
    }
}
