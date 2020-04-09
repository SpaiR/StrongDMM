package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.controller.shortcut.ShortcutHandler
import strongdmm.controller.tool.ToolType
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerToolsController
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class ToolSelectPanelUi : EventConsumer, EventSender, ShortcutHandler() {
    private val tools: Array<ToolType> = ToolType.values()
    private var activeTool: ToolType = ToolType.TILE

    init {
        consumeEvent(Reaction.ActiveToolChanged::class.java, ::handleActiveToolChanged)

        addShortcut(GLFW.GLFW_KEY_1) { selectTool(tools[0]) }
        addShortcut(GLFW.GLFW_KEY_2) { selectTool(tools[1]) }
        addShortcut(GLFW.GLFW_KEY_3) { selectTool(tools[2]) }
    }

    fun process() {
        setNextWindowPos(350f, 30f, AppWindow.defaultWindowCond)
        setNextWindowSize(80f, 35f, AppWindow.defaultWindowCond)

        window("Tool", ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize) {
            tools.forEach { tool ->
                val isToolActive = tool == activeTool

                if (isToolActive) {
                    pushStyleColor(ImGuiCol.Button, 0f, .5f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonHovered, 0f, .8f, 0f, 1f)
                    pushStyleColor(ImGuiCol.ButtonActive, 0f, .5f, 0f, 1f)
                }

                button(tool.toolName) {
                    sendEvent(TriggerToolsController.ChangeTool(tool))
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
        sendEvent(TriggerToolsController.ChangeTool(tool))
    }

    private fun handleActiveToolChanged(event: Event<ToolType, Unit>) {
        activeTool = event.body
    }
}
