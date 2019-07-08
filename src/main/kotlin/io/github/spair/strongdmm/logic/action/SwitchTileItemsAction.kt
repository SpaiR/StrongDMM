package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class SwitchTileItemsAction(
    private val tile: Tile,
    private val item1: Int,
    private val item2: Int
) : Undoable {
    override fun doAction(): Undoable {
        tile.switchTileItems(item1, item2)
        Frame.update(true)
        return SwitchTileItemsAction(tile, item2, item1)
    }
}
