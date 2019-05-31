package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.history.History
import io.github.spair.strongdmm.logic.history.MultipleAction
import io.github.spair.strongdmm.logic.history.TileReplaceAction
import io.github.spair.strongdmm.logic.history.Undoable

object TileOperation {

    private val tilesBuffer = mutableMapOf<CoordPoint, List<TileItem>>()

    fun hasTileInBuffer() = tilesBuffer.isNotEmpty()

    fun cut(map: Dmm, tile: Tile) {
        cut(map, listOf(tile))
    }

    fun cut(map: Dmm, tiles: Collection<Tile>) {
        copy(tiles)
        delete(map, tiles)
    }

    fun copy(tile: Tile) {
        copy(listOf(tile))
    }

    fun copy(tiles: Collection<Tile>) {
        tilesBuffer.clear()
        tiles.forEach { tilesBuffer[CoordPoint(it.x, it.y)] = it.getVisibleTileItems() }
    }

    fun paste(map: Dmm, x: Int, y: Int, areaAction: (coordArea: CoordArea) -> Unit) {
        if (tilesBuffer.isEmpty()) {
            return
        }

        val reverseActions = mutableListOf<Undoable>()

        val initialCoordArea = getAreaOfTiles(tilesBuffer.keys)
        val coordArea = initialCoordArea.shiftToPoint(x, y)

        areaAction(coordArea)

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

                    val tileItemsBefore = tile.getTileItems()
                    tile.replaceVisibleTileItems(newTileItems)
                    reverseActions.add(TileReplaceAction(map, tile.x, tile.y, tileItemsBefore, tile.getTileItems()))
                }
            }
        }

        History.addUndoAction(MultipleAction(reverseActions))
    }

    fun delete(map: Dmm, tile: Tile) {
        delete(map, listOf(tile))
    }

    fun delete(map: Dmm, tiles: Collection<Tile>) {
        val reverseActions = mutableListOf<Undoable>()

        tiles.forEach { tile ->
            val tileItemsBefore = tile.getTileItems()
            tile.clearVisibleTile()
            reverseActions.add(TileReplaceAction(map, tile.x, tile.y, tileItemsBefore, tile.getTileItems()))
        }

        History.addUndoAction(MultipleAction(reverseActions))
    }
}
