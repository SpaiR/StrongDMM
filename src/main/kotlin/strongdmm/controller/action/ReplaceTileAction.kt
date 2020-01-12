package strongdmm.controller.action

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.Tile

class ReplaceTileAction(
    private val dmm: Dmm,
    private val x: Int,
    private val y: Int,
    tileChangeAction: () -> Unit
) : Undoable {
    constructor(tile: Tile, tileChangeAction: () -> Unit) : this(tile.dmm, tile.x, tile.y, tileChangeAction)

    private val tileItemsIdBefore: LongArray = dmm.getTileItemsId(x, y).copyOf()
    private val tileItemsIdAfter: LongArray

    init {
        tileChangeAction()
        tileItemsIdAfter = dmm.getTileItemsId(x, y).copyOf()
    }

    override fun doAction(): Undoable = ReplaceTileAction(dmm, x, y) {
        dmm.setTileItemsId(x, y, tileItemsIdBefore)
    }
}
