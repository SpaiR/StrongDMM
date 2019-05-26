package io.github.spair.strongdmm.logic.map

object TileOperation {

    private val tilesBuffer = mutableMapOf<CoordPoint, List<TileItem>>()

    fun hasTileInBuffer() = tilesBuffer.isNotEmpty()

    fun cut(tile: Tile) {
        copy(tile)
        tile.clearTile()
    }

    fun cut(tiles: Collection<Tile>) {
        copy(tiles)
        tiles.forEach(Tile::clearTile)
    }

    fun copy(tile: Tile) {
        copy(listOf(tile))
    }

    fun copy(tiles: Collection<Tile>) {
        tilesBuffer.clear()
        tiles.forEach { tilesBuffer[CoordPoint(it.x, it.y)] = it.getVisibleTileItems() }
    }

    fun paste(map: Dmm, x: Int, y: Int) {
        if (tilesBuffer.isEmpty()) {
            return
        }

        val initialCoordArea = getAreaOfTiles(tilesBuffer.keys)
        val coordArea = initialCoordArea.shiftToPoint(x, y)

        val xInit = initialCoordArea.x1
        val yInit = initialCoordArea.y1

        for ((yIndex, yTile) in (coordArea.y1..coordArea.y2).withIndex()) {
            for ((xIndex, xTile) in (coordArea.x1..coordArea.x2).withIndex()) {
                map.getTile(xTile, yTile)?.let { tile ->
                    val tileItems = tilesBuffer[CoordPoint(xInit + xIndex, yInit + yIndex)]!!
                    val newTileItems = mutableListOf<TileItem>()

                    tileItems.forEach { tileItem ->
                        newTileItems.add(TileItem.fromTileItem(tileItem, xTile, yTile))
                    }

                    tile.replaceTileItems(newTileItems)
                }
            }
        }
    }

    fun delete(tile: Tile) {
        tile.clearTile()
    }

    fun delete(tiles: Collection<Tile>) {
        tiles.forEach(Tile::clearTile)
    }
}
