package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.action.ActionController
import io.github.spair.strongdmm.logic.action.MultipleAction
import io.github.spair.strongdmm.logic.action.TileReplaceAction
import io.github.spair.strongdmm.logic.action.Undoable
import kotlin.math.max
import kotlin.math.min

object TileOperation {

    private val tilesBuffer: MutableMap<CoordPoint, IntArray> = hashMapOf()

    fun hasTileInBuffer(): Boolean = tilesBuffer.isNotEmpty()

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
        tiles.forEach { tilesBuffer[CoordPoint(it.x, it.y)] = it.getVisibleTileItemsIDs() }
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
                    val tileItemsIDs = tilesBuffer[CoordPoint(xInit + xIndex, yInit + yIndex)]!!
                    val tileItemsIDsBefore = tile.getTileItemsIDs()
                    tile.replaceOnlyVisibleTileItemsByIDs(tileItemsIDs)
                    reverseActions.add(TileReplaceAction(map, xTile, yTile, tileItemsIDsBefore, tile.getTileItemsIDs()))
                }
            }
        }

        ActionController.addUndoAction(MultipleAction(reverseActions))
    }

    fun delete(map: Dmm, tile: Tile) {
        delete(map, listOf(tile))
    }

    fun delete(map: Dmm, tiles: Collection<Tile>) {
        val reverseActions = mutableListOf<Undoable>()

        tiles.forEach { tile ->
            val tileItemsIDsBefore = tile.getTileItemsIDs()
            tile.deleteVisibleTileItems()
            reverseActions.add(TileReplaceAction(map, tile.x, tile.y, tileItemsIDsBefore, tile.getTileItemsIDs()))
        }

        ActionController.addUndoAction(MultipleAction(reverseActions))
    }

    private fun getAreaOfTiles(tilesCoords: Set<CoordPoint>): CoordArea {
        var x1 = Int.MAX_VALUE
        var y1 = Int.MAX_VALUE
        var x2 = Int.MIN_VALUE
        var y2 = Int.MIN_VALUE

        tilesCoords.forEach {
            x1 = min(x1, it.x)
            y1 = min(y1, it.y)
            x2 = max(x2, it.x)
            y2 = max(y2, it.y)
        }

        return CoordArea(x1, y1, x2, y2)
    }
}
