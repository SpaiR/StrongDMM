package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class SwapTileItemsAction(
    private val tile: Tile,
    private val itemIdx1: Int,
    private val itemIdx2: Int
) : Undoable {
    override fun doAction(): Undoable {
        tile.swapTileItems(itemIdx1, itemIdx2)
        Frame.update(true)
        return SwapTileItemsAction(tile, itemIdx2, itemIdx1)
    }
}
