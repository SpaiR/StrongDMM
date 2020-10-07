package strongdmm.ui.menu_bar

import imgui.type.ImBoolean
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.service.action.ActionStatus

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        EventBus.sign(Reaction.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        EventBus.sign(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        EventBus.sign(Reaction.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        EventBus.sign(Reaction.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)

        EventBus.sign(Provider.InstanceLocatorPanelUiOpen::class.java, ::handleProviderInstanceLocatorPanelUiOpen)
        EventBus.sign(Provider.CanvasServiceFrameAreas::class.java, ::handleProviderCanvasServiceFrameAreas)
        EventBus.sign(Provider.CanvasServiceSynchronizeMapsView::class.java, ::handleProviderCanvasServiceSynchronizeMapsView)
        EventBus.sign(Provider.RecentFilesServiceRecentEnvironments::class.java, ::handleProviderRecentFilesServiceRecentEnvironments)
        EventBus.sign(Provider.RecentFilesServiceRecentMaps::class.java, ::handleProviderRecentFilesServiceRecentMaps)
    }

    private fun handleEnvironmentLoadStarted() {
        state.isLoadingEnvironment = true
    }

    private fun handleEnvironmentLoadStopped() {
        state.isLoadingEnvironment = false
    }

    private fun handleEnvironmentChanged() {
        state.isEnvironmentOpened = true
    }

    private fun handleEnvironmentReset() {
        state.isEnvironmentOpened = false
    }

    private fun handleSelectedMapChanged() {
        state.isMapOpened = true
    }

    private fun handleSelectedMapClosed() {
        state.isMapOpened = false
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        state.isUndoEnabled = event.body.hasUndoAction
        state.isRedoEnabled = event.body.hasRedoAction
    }

    private fun handleLayersFilterRefreshed(event: Event<Set<String>, Unit>) {
        state.isAreaLayerActive.set(!event.body.contains(TYPE_AREA))
        state.isTurfLayerActive.set(!event.body.contains(TYPE_TURF))
        state.isObjLayerActive.set(!event.body.contains(TYPE_OBJ))
        state.isMobLayerActive.set(!event.body.contains(TYPE_MOB))
    }

    private fun handleProviderInstanceLocatorPanelUiOpen(event: Event<ImBoolean, Unit>) {
        state.providedShowInstanceLocator = event.body
    }

    private fun handleProviderCanvasServiceFrameAreas(event: Event<ImBoolean, Unit>) {
        state.providedFrameAreas = event.body
    }

    private fun handleProviderCanvasServiceSynchronizeMapsView(event: Event<ImBoolean, Unit>) {
        state.providedSynchronizeMapsView = event.body
    }

    private fun handleProviderRecentFilesServiceRecentEnvironments(event: Event<List<String>, Unit>) {
        state.providedRecentEnvironments = event.body
    }

    private fun handleProviderRecentFilesServiceRecentMaps(event: Event<List<MapPath>, Unit>) {
        state.providedRecentMaps = event.body
    }
}
