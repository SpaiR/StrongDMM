package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiCol
import imgui.enums.ImGuiWindowFlags
import org.lwjgl.glfw.GLFW
import strongdmm.byond.dmm.Dmm
import strongdmm.controller.shortcut.Shortcut
import strongdmm.controller.shortcut.ShortcutHandler
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.Reaction
import strongdmm.event.type.controller.TriggerMapHolderController
import strongdmm.util.imgui.GREY32
import strongdmm.util.imgui.setItemHoveredTooltip
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class LevelSwitchPanelUi : ShortcutHandler(), EventConsumer, EventSender {
    private var selectedMap: Dmm? = null

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)

        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_LEFT, action = ::doDecreaseActiveZ)
        addShortcut(Shortcut.CONTROL_PAIR, GLFW.GLFW_KEY_RIGHT, action = ::doIncreaseActiveZ)
    }

    fun process() {
        if (selectedMap == null || selectedMap!!.maxZ == 1) {
            return
        }

        setNextWindowPos(AppWindow.windowWidth - 100f, AppWindow.windowHeight - 75f, AppWindow.defaultWindowCond)
        setNextWindowSize(90f, 10f)

        window("lavel_switch_panel", ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            selectedMap?.let { map ->
                if (map.zActive == 1) {
                    showDisabledSwitch("<")
                } else {
                    if (smallButton("<")) {
                        doDecreaseActiveZ()
                    }
                    setItemHoveredTooltip("Prev Z level (Ctrl+Left Arrow)")
                }

                sameLine()
                text("Z:${map.zActive}")
                sameLine()

                if (map.zActive == map.maxZ) {
                    showDisabledSwitch(">")
                } else {
                    if (smallButton(">")) {
                        doIncreaseActiveZ()
                    }
                    setItemHoveredTooltip("Next Z level (Ctrl+Right Arrow)")
                }
            }
        }
    }

    private fun showDisabledSwitch(switchSymbol: String) {
        pushStyleColor(ImGuiCol.Button, GREY32)
        pushStyleColor(ImGuiCol.ButtonActive, GREY32)
        pushStyleColor(ImGuiCol.ButtonHovered, GREY32)
        smallButton(switchSymbol)
        popStyleColor(3)
    }

    private fun doDecreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeActiveZ(selectedMap!!.zActive - 1))
    }

    private fun doIncreaseActiveZ() {
        sendEvent(TriggerMapHolderController.ChangeActiveZ(selectedMap!!.zActive + 1))
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        selectedMap = event.body
    }

    private fun handleEnvironmentReset() {
        selectedMap = null
    }

    private fun handleSelectedMapClosed() {
        selectedMap = null
    }
}
