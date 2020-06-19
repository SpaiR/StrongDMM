package strongdmm.ui.panel.changelog

import imgui.ImGui
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.window

class View(
    private val state: State
) {
    companion object {
        private const val WIDTH: Float = 800f
        private const val HEIGHT: Float = 500f

        private const val TITLE: String = "Changelog"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextPosAndSizeCentered(WIDTH, HEIGHT)

        window(TITLE, state.isOpened) {
            ImGui.textWrapped(state.providedChangelogText)
        }
    }
}
