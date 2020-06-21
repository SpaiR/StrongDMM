package strongdmm.ui.panel.unknown_types

import imgui.ImGui.*
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 300f * Window.pointSize
        private val height: Float
            get() = 450f * Window.pointSize

        private const val TITLE: String = "Unknown Types"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        window(TITLE, state.isOpened) {
            textWrapped("There are unknown types on the map. They were removed.")
            separator()
            columns(2, "unknown_types_columns", true)

            state.unknownTypes.forEach { (map_pos, type) ->
                text("X:${map_pos.x} Y:${map_pos.y} Z:${map_pos.z}")
                nextColumn()
                text(type)
                nextColumn()
            }
        }
    }
}
