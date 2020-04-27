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
        consumeEvent(Reaction.EnvironmentLoading::class.java, ::handleEnvironmentLoading)
        consumeEvent(Reaction.EnvironmentLoaded::class.java, ::handleEnvironmentLoaded)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.ActiveTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Provider.RecentFilesControllerRecentEnvironments::class.java, ::handleRecentFilesControllerRecentEnvironments)
    }

    private fun handleEnvironmentLoading() {
        state.isEnvironmentLoading = true
    }

    private fun handleEnvironmentLoaded() {
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

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        state.activeTileItemType = event.body?.type ?: ""
    }

    private fun handleRecentFilesControllerRecentEnvironments(event: Event<List<String>, Unit>) {
        state.providedRecentEnvironments = event.body
    }
}
