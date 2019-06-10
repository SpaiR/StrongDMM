package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.instancelist.InstanceListView
import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Tile

class SwapTileItemAction(
    private val tile: Tile,
    private val idBefore: Int,
    private val idAfter: Int
) : Undoable {
    override fun doAction(): Undoable {
        tile.swapTileItem(idBefore, idAfter)
        Frame.update(true)
        InstanceListView.updateSelectedInstanceInfo()
        return SwapTileItemAction(tile, idAfter, idBefore)
    }
}
