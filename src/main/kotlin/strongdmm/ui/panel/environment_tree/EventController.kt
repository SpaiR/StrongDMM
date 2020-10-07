package strongdmm.ui.panel.environment_tree

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.MapPath
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        EventBus.sign(Reaction.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        EventBus.sign(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(Provider.RecentFilesServiceRecentEnvironmentsWithMaps::class.java, ::handleRecentFilesServiceRecentEnvironmentsWithMaps)
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

        if (!state.isSelectedInCycle) {
            state.isDoOpenSelectedType = true
        }
    }

    private fun handleRecentFilesServiceRecentEnvironmentsWithMaps(event: Event<Map<String, List<MapPath>>, Unit>) {
        state.providedRecentEnvironmentsWithMaps = event.body.toMap().mapValues { it.value.toList() }
    }
}
