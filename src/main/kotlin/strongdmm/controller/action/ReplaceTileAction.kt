package strongdmm.controller.action

import strongdmm.byond.dmm.Tile

class ReplaceTileAction(
    private val tile: Tile,
    tileChangeAction: () -> Unit
) : Undoable {
    private val tileItemsIdBefore: LongArray = tile.getTileItemsId().copyOf()
    private val tileItemsIdAfter: LongArray

    init {
        tileChangeAction()
        tileItemsIdAfter = tile.getTileItemsId().copyOf()
    }

    override fun doAction(): Undoable = ReplaceTileAction(tile) {
        tile.replaceTileItemsId(tileItemsIdBefore)
    }
}
