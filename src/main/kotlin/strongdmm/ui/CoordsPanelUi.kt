package strongdmm.ui

import imgui.ImGui.*
import imgui.enums.ImGuiWindowFlags
import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.type.Reaction
import strongdmm.util.OUT_OF_BOUNDS
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class CoordsPanelUi : EventConsumer {
    private var isMapOpened: Boolean = false

    private var xMapMousePos: Int = OUT_OF_BOUNDS
    private var yMapMousePos: Int = OUT_OF_BOUNDS

    init {
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.MapMousePosChanged::class.java, ::handleMapMousePosChanged)
    }

    fun process() {
        if (!isMapOpened) {
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

    private fun handleSelectedMapChanged() {
        isMapOpened = true
    }

    private fun handleSelectedMapClosed() {
        isMapOpened = false
    }

    private fun handleEnvironmentReset() {
        isMapOpened = false
    }

    private fun handleMapMousePosChanged(event: Event<MapPos, Unit>) {
        xMapMousePos = event.body.x
        yMapMousePos = event.body.y
    }
}
