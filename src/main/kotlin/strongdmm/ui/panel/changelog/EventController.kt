package strongdmm.ui.panel.changelog

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerChangelogPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Provider.ChangelogServiceChangelogText::class.java, ::handleProviderChangelogServiceChangelogText)
        consumeEvent(TriggerChangelogPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderChangelogServiceChangelogText(event: Event<String, Unit>) {
        state.providedChangelogText = event.body
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
