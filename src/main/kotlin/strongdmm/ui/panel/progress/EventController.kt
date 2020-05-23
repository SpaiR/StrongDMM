package strongdmm.ui.panel.progress

import imgui.ImGui
import imgui.ImVec2
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import java.io.File

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        consumeEvent(Reaction.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        consumeEvent(Reaction.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        consumeEvent(Reaction.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)
    }

    private fun handleEnvironmentLoadStarted(event: Event<File, Unit>) {
        setProgressText("Loading " + event.body.absolutePath.replace('\\', '/').substringAfterLast("/"))
    }

    private fun handleEnvironmentLoadStopped() {
        state.progressText = null
    }

    private fun handleScreenshotTakeStarted() {
        setProgressText("Screenshot")
    }

    private fun handleScreenshotTakeStopped() {
        state.progressText = null
    }

    private fun setProgressText(text: String) {
        val dst = ImVec2()
        ImGui.calcTextSize(dst, "···· $text ····")
        state.progressText = text
        state.progressTextWidth = dst.x
    }
}
