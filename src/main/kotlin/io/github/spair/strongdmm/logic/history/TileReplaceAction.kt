package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem

class TileReplaceAction(
    private val map: Dmm,
    private val x: Int,
    private val y: Int,
    private val tileObjectsBefore: List<TileItem>,
    private val tileObjectsAfter: List<TileItem>
) : Undoable {
    override fun doAction(): Undoable {
        val tile = map.getTile(x, y)!!
        val reverseAction = TileReplaceAction(map, x, y, tileObjectsAfter, tileObjectsBefore)
        tile.replaceTileItems(tileObjectsBefore)
        Frame.update(true)
        return reverseAction
    }
}
