package io.github.spair.strongdmm.logic.action

import io.github.spair.strongdmm.gui.map.Frame
import io.github.spair.strongdmm.logic.map.Dmm
import io.github.spair.strongdmm.logic.map.Tile
import io.github.spair.strongdmm.logic.map.extension.addTile

class ChangeMapSizeAction(
    private val map: Dmm,
    private val prevMaxX: Int,
    private val prevMaxY: Int,
    private val deletedTiles: List<Tile>
) : Undoable {
    override fun doAction(): Undoable {
        val maxX = map.getMaxX()
        val maxY = map.getMaxY()
        val tiles = map.changeMapSize(prevMaxX, prevMaxY)

        deletedTiles.forEach { tile ->
            map.getTile(tile.x, tile.y)!!.fullReplaceTileItemsByIDs(tile.unsafeTileItemsIDs())
            map.initialDmmData.addTile(tile.x, tile.y, tile.getTileContent())
        }

        Frame.update(true)
        return ChangeMapSizeAction(map, maxX, maxY, tiles)
    }
}
