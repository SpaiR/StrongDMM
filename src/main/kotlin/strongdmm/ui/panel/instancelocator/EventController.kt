package strongdmm.ui.panel.instancelocator

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.ReactionEnvironmentService
import strongdmm.event.service.ReactionMapHolderService
import strongdmm.event.ui.ProviderInstanceLocatorPanelUi
import strongdmm.event.ui.TriggerInstanceLocatorPanelUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionMapHolderService.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(TriggerInstanceLocatorPanelUi.SearchByType::class.java, ::handleSearchByType)
        EventBus.sign(TriggerInstanceLocatorPanelUi.SearchById::class.java, ::handleSearchById)
    }

    lateinit var viewController: ViewController

    fun postInit() {
        EventBus.post(ProviderInstanceLocatorPanelUi.DoInstanceLocatorOpen(state.doInstanceLocatorOpen))
    }

    private fun handleEnvironmentReset() {
        state.searchType.set("")
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.mapMaxX = event.body.maxX
        state.mapMaxY = event.body.maxY

        if (state.searchX1.get() > state.mapMaxX) {
            state.searchX1.set(1)
        }

        if (state.searchY1.get() > state.mapMaxX) {
            state.searchY1.set(1)
        }

        if (state.searchX2.get() > state.mapMaxX) {
            state.searchX2.set(state.mapMaxX)
        }

        if (state.searchY2.get() > state.mapMaxX) {
            state.searchY2.set(state.mapMaxY)
        }
    }

    private fun handleSearchByType(event: Event<String, Unit>) {
        state.doInstanceLocatorOpen.set(true)
        state.searchType.set(event.body)
        viewController.doSearch()
    }

    private fun handleSearchById(event: Event<Long, Unit>) {
        state.doInstanceLocatorOpen.set(true)
        state.searchType.set(event.body.toString())
        viewController.doSearch()
    }
}
