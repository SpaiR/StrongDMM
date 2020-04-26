package strongdmm.ui.about_panel

import strongdmm.event.EventHandler
import strongdmm.event.type.ui.TriggerAboutPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerAboutPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
