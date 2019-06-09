package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem

class DeleteTileItemAction(private val map: Dmm, private val x: Int, private val y: Int, private val tileItem: TileItem) : Undoable {
    override fun doAction(): Undoable {
        map.getTile(x, y)?.let {
            it.deleteTileItem(tileItem)
            Frame.update(true)
        }
        return PlaceTileItemAction(map, x, y, tileItem)
    }
}
