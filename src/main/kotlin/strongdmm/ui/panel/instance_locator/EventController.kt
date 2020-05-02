package strongdmm.ui.panel.instance_locator

import strongdmm.byond.dmm.Dmm
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerInstanceLocatorPanelUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(TriggerInstanceLocatorPanelUi.SearchByType::class.java, ::handleSearchByType)
        consumeEvent(TriggerInstanceLocatorPanelUi.SearchById::class.java, ::handleSearchById)
    }

    lateinit var viewController: ViewController

    fun postInit() {
        sendEvent(Provider.InstanceLocatorPanelUiOpen(state.showInstanceLocator))
    }

    private fun handleEnvironmentReset() {
        state.searchType.set("")
    }

    private fun handleSelectedMapChanged(event: Event<Dmm, Unit>) {
        state.mapMaxX = event.body.maxX
        state.mapMaxY = event.body.maxY

        state.searchX2.set(state.mapMaxX)
        state.searchY2.set(state.mapMaxY)
    }

    private fun handleSearchByType(event: Event<String, Unit>) {
        state.showInstanceLocator.set(true)
        state.searchType.set(event.body)
        viewController.doSearch()
    }

    private fun handleSearchById(event: Event<Long, Unit>) {
        state.showInstanceLocator.set(true)
        state.searchType.set(event.body.toString())
        viewController.doSearch()
    }
}
