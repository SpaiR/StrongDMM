package io.github.spair.strongdmm.logic.map

object TileOperation {

    private var tileObjectsBuffer = mutableListOf<TileItem>()

    fun hasTileInBuffer() = tileObjectsBuffer.isNotEmpty()

    fun cut(map: Dmm, tile: Tile) {
        copy(tile)
        tileObjectsBuffer.forEach { map.deleteTileItem(it) }
    }

    fun copy(tile: Tile) {
        tileObjectsBuffer = tile.tileItems.toMutableList()
    }

    fun paste(map: Dmm, x: Int, y: Int) {
        map.getTile(x, y)?.let { tile ->
            tile.tileItems.clear()
            tileObjectsBuffer.forEach { tileItem ->
                tile.tileItems.add(TileItem(tileItem.dmeItem, x, y, tileItem.customVars.toMutableMap()))
            }
        }
    }

    fun delete(map: Dmm, tile: Tile) {
        tile.tileItems.toList().forEach { map.deleteTileItem(it) }
    }
}
