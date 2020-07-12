package strongdmm.ui.panel.changelog

import strongdmm.util.imgui.ImGuiUtil
import strongdmm.util.imgui.markdown.ImGuiMarkdown
import strongdmm.util.imgui.window
import strongdmm.window.Window

class View(
    private val state: State
) {
    companion object {
        private val width: Float
            get() = 800f * Window.pointSize
        private val height: Float
            get() = 500f * Window.pointSize

        private const val TITLE: String = "Changelog"
    }

    fun process() {
        if (!state.isOpened.get()) {
            return
        }

        ImGuiUtil.setNextWindowCentered(width, height)

        window(TITLE, state.isOpened) {
            ImGuiMarkdown.render(state.providedChangelogMarkdown)
        }
    }
}
