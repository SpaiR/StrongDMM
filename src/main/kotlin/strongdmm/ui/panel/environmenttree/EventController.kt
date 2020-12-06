package strongdmm.ui.panel.environmenttree

import strongdmm.byond.dme.Dme
import strongdmm.byond.dmm.MapPath
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.service.ProviderDmiService
import strongdmm.event.type.service.ProviderRecentFilesService
import strongdmm.event.type.service.ReactionEnvironmentService
import strongdmm.event.type.service.ReactionTileItemService
import strongdmm.service.dmi.DmiCache

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(ReactionEnvironmentService.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        EventBus.sign(ReactionEnvironmentService.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        EventBus.sign(ReactionEnvironmentService.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionTileItemService.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(ProviderDmiService.DmiCache::class.java, ::handleProviderDmiCache)
        EventBus.sign(ProviderRecentFilesService.RecentEnvironmentsWithMaps::class.java, ::handleProviderRecentEnvironmentsWithMaps)
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

    private fun handleProviderDmiCache(event: Event<DmiCache, Unit>) {
        state.providedDmiCache = event.body
    }

    private fun handleProviderRecentEnvironmentsWithMaps(event: Event<Map<String, List<MapPath>>, Unit>) {
        state.providedRecentEnvironmentsWithMaps = event.body.toMap().mapValues { it.value.toList() }
    }
}
