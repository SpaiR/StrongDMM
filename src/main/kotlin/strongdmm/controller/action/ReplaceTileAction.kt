package strongdmm.controller.action

import strongdmm.byond.dmm.Tile

class ReplaceTileAction(
    private val tile: Tile,
    tileChangeAction: () -> Unit
) : Undoable {
    private val tileItemsIdBefore: IntArray = tile.getTileItemsId().copyOf()
    private val tileItemsIdAfter: IntArray

    init {
        tileChangeAction()
        tileItemsIdAfter = tile.getTileItemsId().copyOf()
    }

    override fun doAction(): Undoable = ReplaceTileAction(tile) {
        tile.replaceTileItemsId(tileItemsIdBefore)
    }
}
