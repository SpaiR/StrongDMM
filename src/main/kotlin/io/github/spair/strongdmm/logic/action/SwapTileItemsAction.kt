package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class SwapTileItemsAction(
    private val tile: Tile,
    private val item1: Int,
    private val item2: Int
) : Undoable {
    override fun doAction(): Undoable {
        tile.swapTileItems(item1, item2)
        Frame.update(true)
        return SwapTileItemsAction(tile, item2, item1)
    }
}
