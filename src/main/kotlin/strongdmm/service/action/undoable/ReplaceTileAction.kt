package strongdmm.service.action.undoable

import strongdmm.byond.dmm.Dmm
import strongdmm.byond.dmm.Tile

class ReplaceTileAction(
    private val dmm: Dmm,
    private val x: Int,
    private val y: Int,
    private val z: Int,
    tileChangeAction: () -> Unit
) : Undoable {
    constructor(tile: Tile, tileChangeAction: () -> Unit) : this(tile.dmm, tile.x, tile.y, tile.z, tileChangeAction)

    private val tileItemsIdBefore: LongArray = dmm.getTileItemsId(x, y, z).copyOf()
    private val tileItemsIdAfter: LongArray

    init {
        tileChangeAction()
        tileItemsIdAfter = dmm.getTileItemsId(x, y, z).copyOf()
    }

    override fun doAction(): Undoable {
        return ReplaceTileAction(dmm, x, y, z) {
            dmm.setTileItemsId(x, y, z, tileItemsIdBefore)
        }
    }
}
