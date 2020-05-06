package strongdmm.ui.panel.environment_tree

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        consumeEvent(Reaction.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        consumeEvent(Provider.RecentFilesControllerRecentEnvironments::class.java, ::handleRecentFilesControllerRecentEnvironments)
    }

    private fun handleEnvironmentLoadStarted() {
        state.isEnvironmentLoading = true
    }

    private fun handleEnvironmentLoadStopped() {
        state.isEnvironmentLoading = false
    }

    private fun handleEnvironmentChanged(event: Event<Dme, Unit>) {
        state.currentEnvironment = event.body
    }

    private fun handleEnvironmentReset() {
        state.typeFilter.set("")
        state.currentEnvironment = null
        state.treeNodes.clear()
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        state.selectedTileItemType = event.body?.type ?: ""
    }

    private fun handleRecentFilesControllerRecentEnvironments(event: Event<List<String>, Unit>) {
        state.providedRecentEnvironments = event.body
    }
}
