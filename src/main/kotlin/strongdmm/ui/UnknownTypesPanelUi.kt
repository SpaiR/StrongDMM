package strongdmm.ui

import imgui.ImBool
import imgui.ImGui.*
import strongdmm.byond.dmm.MapPos
import strongdmm.event.Event
import strongdmm.event.EventConsumer
import strongdmm.event.EventSender
import strongdmm.event.type.ui.TriggerUnknownTypesPanelUi
import strongdmm.util.imgui.window
import strongdmm.window.AppWindow

class UnknownTypesPanelUi : EventConsumer, EventSender {
    private val isOpened: ImBool = ImBool(false)
    private lateinit var unknownTypes: List<Pair<MapPos, String>>

    init {
        consumeEvent(TriggerUnknownTypesPanelUi.Open::class.java, ::handleOpen)
    }

    fun process() {
        if (!isOpened.get()) {
            return
        }

        setNextWindowPos(AppWindow.windowWidth / 2 - 200f, AppWindow.windowHeight / 2 - 225f, AppWindow.defaultWindowCond)
        setNextWindowSize(300f, 450f, AppWindow.defaultWindowCond)

        window("Unknown Types", isOpened) {
            textWrapped("There are unknown types on the map. They were removed.")
            separator()
            columns(2, "unknown_types_columns", true)

            unknownTypes.forEach { (map_pos, type) ->
                text("X:${map_pos.x} Y:${map_pos.y} Z:${map_pos.z}")
                nextColumn()
                text(type)
            }
        }
    }

    private fun handleOpen(event: Event<List<Pair<MapPos, String>>, Unit>) {
        unknownTypes = event.body
        isOpened.set(true)
    }
}
