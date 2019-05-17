package io.github.spair.strongdmm.logic.history

import io.github.spair.strongdmm.gui.mapcanvas.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.TileItem

class PlaceTileItemAction(private val map: Dmm, private val tileItem: TileItem) : Undoable {
    override fun doAction(): Undoable {
        map.placeTileItem(tileItem)
        Frame.update(true)
        return DeleteTileItemAction(map, tileItem)
    }
}
