package strongdmm.service.action.undoable

import strongdmm.byond.dmm.GlobalTileItemHolder
import strongdmm.byond.dmm.TileItem

class TileItemRemoveAction(
    private val tileItem: TileItem,
    private val updateAction: () -> Unit
) : Undoable {
    init {
        updateAction()
    }

    override fun doAction(): Undoable {
        GlobalTileItemHolder.remove(tileItem)
        return TileItemAddAction(tileItem, updateAction)
    }
}
