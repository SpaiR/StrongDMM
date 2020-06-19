package strongdmm.ui.panel.about

import imgui.ImGui.textWrapped
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.window

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 550f
        private const val HEIGHT: Float = 170f

        private const val TITLE: String = "About"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

        window(TITLE, state.isOpened) {
            textWrapped(state.aboutText)
        }
    }
}
