package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem

class DeleteTileItemAction(private val map: Dmm, private val tileItem: TileItem) : Undoable {
    override fun doAction(): Undoable {
        val tile = map.getTile(tileItem.x, tileItem.y)!!
        tile.deleteTileItem(tileItem)
        Frame.update(true)
        return PlaceTileItemAction(map, tileItem)
    }
}
