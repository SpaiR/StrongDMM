package strongdmm.ui.panel.search_result

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerSearchResultPanelUi
import strongdmm.ui.panel.search_result.model.SearchResult

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(TriggerSearchResultPanelUi.Open::class.java, ::handleOpen)
    }

    lateinit var viewController: ViewController

    private fun handleEnvironmentReset() {
        viewController.dispose()
    }

    private fun handleSelectedMapChanged() {
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
