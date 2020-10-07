package strongdmm.ui.dialog.edit_vars

import strongdmm.byond.dmm.Tile
import strongdmm.byond.dmm.TileItem
import strongdmm.event.Event
import strongdmm.event.EventBus
import strongdmm.event.type.Reaction
import strongdmm.event.type.ui.TriggerEditVarsDialogUi

class EventController(
    private val state: State
) {
    init {
        EventBus.sign(Reaction.EnvironmentReset::class.java, ::handleEnvironmentReset)
        EventBus.sign(Reaction.SelectedMapChanged::class.java, ::handleSelectedMapChanged)
        EventBus.sign(Reaction.OpenedMapClosed::class.java, ::handleOpenedMapClosed)
        EventBus.sign(TriggerEditVarsDialogUi.OpenWithTile::class.java, ::handleOpenWithTile)
        EventBus.sign(TriggerEditVarsDialogUi.OpenWithTileItem::class.java, ::handleOpenWithTileItem)
    }

    lateinit var viewController: ViewController

    private fun handleEnvironmentReset() {
        viewController.doCancel()
    }

    private fun handleSelectedMapChanged() {
        viewController.doCancel()
    }

    private fun handleOpenedMapClosed() {
        viewController.doCancel()
    }

    private fun handleOpenWithTile(event: Event<Pair<Tile, Int>, Unit>) {
        viewController.open()
        state.currentTile = event.body.first
        state.initialTileItemsId = event.body.first.getTileItemsId().clone()
        state.currentTileItemIndex = event.body.second
        viewController.collectDisplayVariables()
        viewController.collectVariablesByType()
        viewController.collectPinnedVariables()
    }

    private fun handleOpenWithTileItem(event: Event<TileItem, TileItem>) {
        viewController.open()
        state.currentTileItem = event.body
        state.newTileItemEvent = event
        viewController.collectDisplayVariables()
        viewController.collectVariablesByType()
        viewController.collectPinnedVariables()
    }
}
