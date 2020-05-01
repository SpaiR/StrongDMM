package strongdmm.ui.panel.search_result

import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerSearchResultPanelUi
import strongdmm.ui.panel.search_result.model.SearchResult

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapZActiveChanged::class.java, ::handleSelectedMapZActiveChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(TriggerSearchResultPanelUi.Open::class.java, ::handleOpen)
    }

    lateinit var viewController: ViewController

    private fun handleEnvironmentReset() {
        viewController.dispose()
    }

    private fun handleSelectedMapChanged() {
        viewController.dispose()
    }

    private fun handleSelectedMapZActiveChanged() {
        viewController.dispose()
    }

    private fun handleSelectedMapClosed() {
        viewController.dispose()
    }

    private fun handleOpen(event: Event<SearchResult, Unit>) {
        state.isOpen.set(true)
        state.searchResult = event.body
    }
}
