package strongdmm.ui.panel.notification

import imgui.ImGui
import imgui.ImVec2
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.ui.TriggerNotificationPanelUi
import java.time.Duration
import java.util.*
import kotlin.concurrent.schedule

class EventController(
    private val state: State
) {
    companion object {
        private const val TEXT_WRAP: String = "    "
        private const val NOTIFY_SECONDS: Long = 2
    }

    init {
        EventBus.sign(TriggerNotificationPanelUi.Notify::class.java, ::handleNotify)
    }

    private fun handleNotify(event: Event<String, Unit>) {
        val textSize = ImVec2()

        ImGui.calcTextSize(textSize, TEXT_WRAP + event.body + TEXT_WRAP)

        state.notificationText = event.body
        state.notificationTextWidth = textSize.x

        state.clearTask?.cancel()
        state.clearTask = Timer().schedule(Duration.ofSeconds(NOTIFY_SECONDS).toMillis()) {
            state.notificationText = null
            state.clearTask = null
        }
    }
}
