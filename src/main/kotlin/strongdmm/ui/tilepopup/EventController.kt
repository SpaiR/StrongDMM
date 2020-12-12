package strongdmm.ui.tilepopup

import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.service.*
import strongdmm.event.ui.ReactionTilePopupUi
import strongdmm.event.ui.TriggerTilePopupUi
import strongdmm.service.action.ActionStatus
import strongdmm.service.dmi.DmiCache
import strongdmm.service.preferences.Preferences

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(TriggerTilePopupUi.Open::class.java, ::handleOpen)
        EventBus.sign(TriggerTilePopupUi.Close::class.java, ::handleClose)
        EventBus.sign(ReactionEnvironmentService.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(ReactionMapHolderService.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        EventBus.sign(ReactionTileItemService.SelectedTileItemChanged::class.java, ::handleSelectedTileItemChanged)
        EventBus.sign(ReactionActionService.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        EventBus.sign(ProviderPreferencesService.Preferences::class.java, ::handleProviderPreferences)
        EventBus.sign(ProviderDmiService.DmiCache::class.java, ::handleProviderDmiCache)
    }

    lateinit var viewController: ViewController

    private fun handleOpen(event: Event<Tile, Unit>) {
        viewController.dispose()

        state.currentTile = event.body
        state.isDoOpen = true
        state.isDisposed = false

        EventBus.post(ReactionTilePopupUi.TilePopupOpened.SIGNAL)
    }

    private fun handleClose() {
        viewController.dispose()
    }

    private fun handleEnvironmentReset() {
        viewController.dispose()
        state.selectedTileItem = null
    }

    private fun handleOpenedMapClosed() {
        state.currentTile = null
    }

    private fun handleSelectedTileItemChanged(event: Event<TileItem?, Unit>) {
        state.selectedTileItem = event.body
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        state.isUndoEnabled = event.body.hasUndoAction
        state.isRedoEnabled = event.body.hasRedoAction
    }

    private fun handleProviderPreferences(event: Event<Preferences, Unit>) {
        state.providedPreferences = event.body
    }

    private fun handleProviderDmiCache(event: Event<DmiCache, Unit>) {
        state.providedDmiCache = event.body
    }
}
