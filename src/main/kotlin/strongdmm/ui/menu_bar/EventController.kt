package strongdmm.ui.menu_bar

import imgui.type.ImBoolean
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF
import strongdmm.byond.dmm.MapPath
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.service.action.ActionStatus

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(Reaction.EnvironmentLoadStarted::class.java, ::handleEnvironmentLoadStarted)
        consumeEvent(Reaction.EnvironmentLoadStopped::class.java, ::handleEnvironmentLoadStopped)
        consumeEvent(Reaction.EnvironmentChanged::class.java, ::handleEnvironmentChanged)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        consumeEvent(Reaction.SelectedMapClosed::class.java, ::handleSelectedMapClosed)
        consumeEvent(Reaction.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        consumeEvent(Reaction.LayersFilterRefreshed::class.java, ::handleLayersFilterRefreshed)

        consumeEvent(Provider.InstanceLocatorPanelUiOpen::class.java, ::handleProviderInstanceLocatorPanelUiOpen)
        consumeEvent(Provider.CanvasServiceFrameAreas::class.java, ::handleProviderCanvasServiceFrameAreas)
        consumeEvent(Provider.CanvasServiceSynchronizeMapsView::class.java, ::handleProviderCanvasServiceSynchronizeMapsView)
        consumeEvent(Provider.RecentFilesServiceRecentEnvironments::class.java, ::handleProviderRecentFilesServiceRecentEnvironments)
        consumeEvent(Provider.RecentFilesServiceRecentMaps::class.java, ::handleProviderRecentFilesServiceRecentMaps)
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
