package strongdmm.ui.panel.progress

import imgui.ImGui
import imgui.ImVec2
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionScreenshotService
import java.io.File

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        EventBus.sign(ReactionEnvironmentService.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        EventBus.sign(ReactionScreenshotService.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        EventBus.sign(ReactionScreenshotService.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)
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
