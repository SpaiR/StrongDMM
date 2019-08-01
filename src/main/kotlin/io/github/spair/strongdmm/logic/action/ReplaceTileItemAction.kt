package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class ReplaceTileItemAction(
    private val tile: Tile,
    private val whichIdx: Int,
    private val withId: Int
) : Undoable {
    override fun doAction(): Undoable {
        val prevId = tile.replaceTileItem(whichIdx, withId)
        Frame.update(true)
        return ReplaceTileItemAction(tile, whichIdx, prevId)
    }
}
