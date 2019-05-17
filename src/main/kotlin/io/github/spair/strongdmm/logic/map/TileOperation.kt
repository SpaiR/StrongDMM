package io.github.spair.strongdmm.logic.map

object TileOperation {

    private var tileObjectsBuffer = listOf<TileItem>()

    fun hasTileInBuffer() = tileObjectsBuffer.isNotEmpty()

    fun cut(map: Dmm, tile: Tile) {
        copy(tile)
        tileObjectsBuffer.forEach { map.deleteTileItem(it) }
    }

    fun copy(tile: Tile) {
        tileObjectsBuffer = tile.getTileItems()
    }

    fun paste(map: Dmm, x: Int, y: Int) {
        map.getTile(x, y)?.let { tile ->
            tile.clearTileItems()
            tileObjectsBuffer.forEach { tileItem ->
                tile.addTileItem(TileItem(tileItem.dmeItem, x, y, tileItem.customVars))
            }
        }
    }

    fun delete(map: Dmm, tile: Tile) {
        tile.getTileItems().forEach { map.deleteTileItem(it) }
    }
}
