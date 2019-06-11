package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm

class TileReplaceAction(
    private val map: Dmm,
    private val x: Int,
    private val y: Int,
    private val tileItemsIDsBefore: IntArray,
    private val tileItemsIDsAfter: IntArray
) : Undoable {
    override fun doAction(): Undoable {
        val tile = map.getTile(x, y)!!
        val reverseAction = TileReplaceAction(map, x, y, tileItemsIDsAfter, tileItemsIDsBefore)
        tile.fullReplaceTileItemsByIDs(tileItemsIDsBefore)
        Frame.update(true)
        return reverseAction
    }
}
