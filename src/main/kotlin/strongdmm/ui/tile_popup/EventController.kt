package strongdmm.ui.tile_popup

import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.controller.action.ActionStatus
import strongdmm.controller.preferences.Preferences
import strongdmm.event.Event
import strongdmm.event.EventHandler
import strongdmm.event.type.Provider
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerTilePopupUi

class EventController(
    private val state: State
) : EventHandler {
    init {
        consumeEvent(TriggerTilePopupUi.Open::class.java, ::handleOpen)
        consumeEvent(TriggerTilePopupUi.Close::class.java, ::handleClose)
        consumeEvent(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        consumeEvent(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        consumeEvent(Reaction.SelectedTileItemChanged::class.java, ::handleActiveTileItemChanged)
        consumeEvent(Reaction.ActionStatusChanged::class.java, ::handleActionStatusChanged)
        consumeEvent(Provider.PreferencesControllerPreferences::class.java, ::handleProviderPreferencesControllerPreferences)
    }

    lateinit var viewController: ViewController

    private fun handleOpen(event: Event<Tile, Unit>) {
        state.currentTile = event.body
        state.isDoOpen = true
        sendEvent(Reaction.TilePopupOpened())
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

    private fun handleActiveTileItemChanged(event: Event<TileItem?, Unit>) {
        state.selectedTileItem = event.body
    }

    private fun handleActionStatusChanged(event: Event<ActionStatus, Unit>) {
        state.isUndoEnabled = event.body.hasUndoAction
        state.isRedoEnabled = event.body.hasRedoAction
    }

    private fun handleProviderPreferencesControllerPreferences(event: Event<Preferences, Unit>) {
        state.providedPreferences = event.body
    }
}
