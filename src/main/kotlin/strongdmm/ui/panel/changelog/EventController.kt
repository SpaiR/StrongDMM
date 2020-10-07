package strongdmm.ui.panel.changelog

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.ui.TriggerChangelogPanelUi
import strongdmm.util.imgui.markdown.ImMarkdown

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Provider.ChangelogServiceChangelogMarkdown::class.java, ::handleProviderChangelogServiceChangelogMarkdown)
        EventBus.sign(TriggerChangelogPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderChangelogServiceChangelogMarkdown(event: Event<ImMarkdown, Unit>) {
        state.providedChangelogMarkdown = event.body
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
