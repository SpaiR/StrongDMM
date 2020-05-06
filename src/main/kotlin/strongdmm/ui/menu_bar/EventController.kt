package strongdmm.ui.menu_bar

import imgui.ImBool
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
import java.io.File

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
        consumeEvent(Reaction.ScreenshotTakeStarted::class.java, ::handleScreenshotTakeStarted)
        consumeEvent(Reaction.ScreenshotTakeStopped::class.java, ::handleScreenshotTakeStopped)

        consumeEvent(Provider.InstanceLocatorPanelUiOpen::class.java, ::handleProviderInstanceLocatorPanelUiOpen)
        consumeEvent(Provider.CanvasControllerFrameAreas::class.java, ::handleProviderCanvasControllerFrameAreas)
        consumeEvent(Provider.RecentFilesControllerRecentEnvironments::class.java, ::handleProviderRecentFilesControllerRecentEnvironments)
        consumeEvent(Provider.RecentFilesControllerRecentMaps::class.java, ::handleProviderRecentFilesControllerRecentMaps)
    }

    private fun handleEnvironmentLoadStarted(event: Event<File, Unit>) {
        state.progressText = "Loading " + event.body.absolutePath.replace('\\', '/').substringAfterLast("/")
    }

    private fun handleEnvironmentLoadStopped() {
        state.progressText = null
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

    private fun handleScreenshotTakeStarted() {
        state.progressText = "Screenshot"
    }

    private fun handleScreenshotTakeStopped() {
        state.progressText = null
    }

    private fun handleProviderInstanceLocatorPanelUiOpen(event: Event<ImBool, Unit>) {
        state.providedShowInstanceLocator = event.body
    }

    private fun handleProviderCanvasControllerFrameAreas(event: Event<ImBool, Unit>) {
        state.providedFrameAreas = event.body
    }

    private fun handleProviderRecentFilesControllerRecentEnvironments(event: Event<List<String>, Unit>) {
        state.providedRecentEnvironments = event.body
    }

    private fun handleProviderRecentFilesControllerRecentMaps(event: Event<List<MapPath>, Unit>) {
        state.providedRecentMaps = event.body
    }
}
