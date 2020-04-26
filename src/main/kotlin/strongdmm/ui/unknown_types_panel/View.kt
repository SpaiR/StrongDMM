package strongdmm.ui.unknown_types_panel

import imgui.ImGui.*
import strongdmm.util.imgui.WindowUtil
import strongdmm.util.imgui.window

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 300f
        private const val HEIGHT: Float = 450f

        private const val TITLE: String = "Unknown Types"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        WindowUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

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
