package strongdmm.service.action.undoable

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem

class TileItemAddAction(
    private val tileItem: TileItem,
    private val updateAction: () -> Unit
) : Undoable {
    init {
        updateAction()
    }

    override fun doAction(): Undoable {
        GlobalTileItemHolder.tryRestoreTileItem(tileItem.id)
        return TileItemRemoveAction(tileItem, updateAction)
    }
}
