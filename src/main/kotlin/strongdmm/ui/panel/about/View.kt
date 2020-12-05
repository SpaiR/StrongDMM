package strongdmm.ui.panel.about

import imgui.ImGui
import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.imGuiBegin
import strongdmm.application.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 550f * Window.pointSize
        private val height: Float
            get() = 170f * Window.pointSize

        private const val TITLE: String = "About"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        imGuiBegin(TITLE, state.isOpened) {
            ImGui.textWrapped(state.aboutText)
        }
    }
}
