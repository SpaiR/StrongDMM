package strongdmm.ui.panel.changelog

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.util.imgui.markdown.ImMarkdown

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Provider.ChangelogServiceChangelogMarkdown::class.java, ::handleProviderChangelogServiceChangelogMarkdown)
        consumeEvent(TriggerChangelogPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderChangelogServiceChangelogMarkdown(event: Event<ImMarkdown, Unit>) {
        state.providedChangelogMarkdown = event.body
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
