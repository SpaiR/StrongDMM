package strongdmm.ui.panel.searchresult

import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionMapHolderService
import strongdmm.event.ui.TriggerSearchResultPanelUi
import strongdmm.ui.panel.searchresult.model.SearchResult

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(ReactionMapHolderService.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
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
