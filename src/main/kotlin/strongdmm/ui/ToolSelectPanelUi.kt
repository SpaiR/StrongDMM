package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiCond
import imgui.enums.ImGuiWindowFlags
import strongdmm.controller.tool.ToolType
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.util.imgui.button
import strongdmm.util.imgui.window

class ToolSelectPanelUi : EventConsumer, EventSender {
    private var currentTool: ToolType = ToolType.TILE

    init {
        consumeEvent(Event.Global.SwitchUsedTool::class.java, ::handleSwitchUsedTool)
    }

    fun process() {
        setNextWindowPos(350f, 30f, ImGuiCond.Once)
        setNextWindowSize(80f, 35f, ImGuiCond.Once)

        window("Tool", ImGuiWindowFlags.NoTitleBar or ImGuiWindowFlags.NoResize) {
            ToolType.values().forEach { tool ->
                val isToolSelected = tool == currentTool

                if (isToolSelected) {
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

                if (isToolSelected) {
                    popStyleColor(3)
                }

                sameLine()
            }
        }
    }

    private fun handleSwitchUsedTool(event: Event<ToolType, Unit>) {
        currentTool = event.body
    }
}
