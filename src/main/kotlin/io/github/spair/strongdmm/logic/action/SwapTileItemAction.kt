package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class SwapTileItemAction(
    private val tile: Tile,
    private val which: Int,
    private val with: Int
) : Undoable {
    override fun doAction(): Undoable {
        tile.replaceTileItem(which, with)
        Frame.update(true)
        return SwapTileItemAction(tile, with, which)
    }
}
