package strongdmm.ui.panel.changelog

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ProviderChangelogService
import strongdmm.event.ui.TriggerChangelogPanelUi
import strongdmm.util.imgui.markdown.ImMarkdown

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ProviderChangelogService.ChangelogMarkdown::class.java, ::handleProviderChangelogMarkdown)
        EventBus.sign(TriggerChangelogPanelUi.Open::class.java, ::handleOpen)
    }

    private fun handleProviderChangelogMarkdown(event: Event<ImMarkdown, Unit>) {
        state.providedChangelogMarkdown = event.body
    }

    private fun handleOpen() {
        state.isOpened.set(true)
    }
}
