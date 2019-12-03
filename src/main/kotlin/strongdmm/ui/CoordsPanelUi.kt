package strongdmm.ui

import glm_.vec2.Vec2
import glm_.vec2.Vec2i
import imgui.Cond
import imgui.ImGui.setNextWindowPos
import imgui.ImGui.setNextWindowSize
import imgui.ImGui.text
import imgui.WindowFlag
import imgui.dsl.window
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapId
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.util.OUT_OF_BOUNDS

class CoordsPanelUi : EventConsumer {
    private var isHasMap: Boolean = false

    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private var selectedMapId: MapId = MapId.NONE

    init {
        consumeEvent(Event.Global.SwitchMap::class.java, ::handleSwitchMap)
        consumeEvent(Event.Global.ResetEnvironment::class.java, ::handleResetEnvironment)
        consumeEvent(Event.Global.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Event.Global.CloseMap::class.java, ::handleCloseMap)
    }

    fun process(windowWidth: Int, windowHeight: Int) {
        if (!isHasMap) {
            return
        }

        setNextWindowPos(Vec2(windowWidth - 110, windowHeight - 40), Cond.Once)
        setNextWindowSize(Vec2(100, 10))

        window("coords_panel", flags = WindowFlag.NoResize or WindowFlag.NoTitleBar) {
            if (xMapMousePos == OUT_OF_BOUNDS || yMapMousePos == OUT_OF_BOUNDS) {
                text("out of bound")
            } else {
                text("X:%03d Y:%03d", xMapMousePos, yMapMousePos)
            }
        }
    }

    private fun handleSwitchMap(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        isHasMap = true
    }

    private fun handleResetEnvironment() {
        isHasMap = false
    }

    private fun handleMapMousePosChanged(event: Event<Vec2i, Unit>) {
        xMapMousePos = event.body.x
        yMapMousePos = event.body.y
    }

    private fun handleCloseMap(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = MapId.NONE
            isHasMap = false
        }
    }
}
