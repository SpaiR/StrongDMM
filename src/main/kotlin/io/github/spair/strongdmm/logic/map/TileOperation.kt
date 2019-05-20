package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.gui.map.MapView

object TileOperation {

    private var tileObjectsBuffer = listOf<TileItem>()

    fun hasTileInBuffer() = tileObjectsBuffer.isNotEmpty()

    fun cut(tile: Tile) {
        copy(tile)
        tileObjectsBuffer.forEach { tile.deleteTileItem(it) }
    }

    fun copy(tile: Tile) {
        tileObjectsBuffer = tile.getTileItems()
    }

    fun paste(x: Int, y: Int) {
        MapView.getSelectedMap()!!.getTile(x, y)?.let { tile ->
            tile.clearTile()
            tileObjectsBuffer.forEach { tileItem ->
                tile.placeTileItem(TileItem.fromTileItem(tileItem, x, y))
            }
        }
    }

    fun delete(tile: Tile) {
        tile.clearTile()
    }
}
