package strongdmm.ui.panel.about

import strongdmm.event.EventBus
import strongdmm.event.type.ui.TriggerAboutPanelUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerAboutPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
