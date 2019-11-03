package strongdmm.ui

import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Cond
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.dsl.window
import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.Message
import strongdmm.util.OUT_OF_BOUNDS
import imgui.WindowFlag as Wf

class CoordsPanelUi : EventConsumer {
    private var isHasMap: Boolean = false

    private var windowWidth: Int = 0
    private var windowHeight: Int = 0

    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    init {
        consumeEvent(Event.GLOBAL_SWITCH_MAP, ::handleSwitchMap)
        consumeEvent(Event.GLOBAL_RESET_ENVIRONMENT, ::handleResetEnvironment)
        consumeEvent(Event.GLOBAL_UPD_MAP_MOUSE_POS, ::handleUpdMapMousePos)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (!isHasMap) {
            return
        }

        val cond = if (this.windowWidth != windowWidth || this.windowHeight != windowHeight) Cond.Always else Cond.Once

        this.windowWidth = windowWidth
        this.windowHeight = windowHeight

        setNextWindowPos(Vec2(windowWidth - 110, windowHeight - 40), cond)
        setNextWindowSize(Vec2(100, 10))

        window("coords_panel", flags = Wf.NoResize or Wf.NoTitleBar) {
            if (xMapMousePos == OUT_OF_BOUNDS || yMapMousePos == OUT_OF_BOUNDS) {
                text("out of bound")
            } else {
                text("X:%03d Y:%03d", xMapMousePos, yMapMousePos)
            }
        }
    }

    private fun handleSwitchMap(msg: Message<Dmm, Unit>) {
        isHasMap = true
    }

    private fun handleResetEnvironment(msg: Message<Unit, Unit>) {
        isHasMap = false
    }

    private fun handleUpdMapMousePos(msg: Message<Vec2i, Unit>) {
        xMapMousePos = msg.body.x
        yMapMousePos = msg.body.y
    }
}
