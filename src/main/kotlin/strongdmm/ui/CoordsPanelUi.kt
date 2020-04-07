package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Reaction
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class CoordsPanelUi : EventConsumer {
    private var isHasMap: Boolean = false

    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    private var selectedMapId: Int = Dmm.MAP_ID_NONE

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
    }

    fun process() {
        if (!isHasMap) {
            return
        }

        setNextWindowPos(AppWindow.windowWidth - 110f, AppWindow.windowHeight - 40f, AppWindow.defaultWindowCond)
        setNextWindowSize(100f, 10f)

        window("coords_panel", ImGuiWindowFlags.NoResize or ImGuiWindowFlags.NoTitleBar) {
            if (xMapMousePos == OUT_OF_BOUNDS || yMapMousePos == OUT_OF_BOUNDS) {
                text("out of bound")
            } else {
                text("X:%03d Y:%03d".format(xMapMousePos, yMapMousePos))
            }
        }
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        selectedMapId = event.body.id
        isHasMap = true
    }

    private fun handleEnvironmentReset() {
        isHasMap = false
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        xMapMousePos = event.body.x
        yMapMousePos = event.body.y
    }

    private fun handleOpenedMapClosed(event: Event<Dmm, Unit>) {
        if (selectedMapId == event.body.id) {
            selectedMapId = Dmm.MAP_ID_NONE
            isHasMap = false
        }
    }
}
