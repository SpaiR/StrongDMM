package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.DmmData
import io.github.spair.dmm.io.TileContent
import io.github.spair.dmm.io.TileLocation
import io.github.spair.strongdmm.common.DEFAULT_ICON_SIZE
import io.github.spair.strongdmm.common.NON_EXISTENT_INT
import io.github.spair.strongdmm.common.TYPE_WORLD
import io.github.spair.strongdmm.common.VAR_ICON_SIZE
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.action.DeleteTileItemAction
import io.github.spair.strongdmm.logic.action.PlaceTileItemAction
import io.github.spair.strongdmm.logic.action.Undoable
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.map.extension.addTile
import io.github.spair.strongdmm.logic.map.extension.deleteTile
import java.io.File

class Dmm(mapFile: File, val initialDmmData: DmmData, dme: Dme) {

    val mapName: String = mapFile.nameWithoutExtension
    val mapPath: String = mapFile.path

    val iconSize: Int

    private var maxX: Int = initialDmmData.maxX
    private var maxY: Int = initialDmmData.maxY
    private var tiles: Array<Array<Tile?>>

    init {
        dme.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE).let {
            iconSize = if (it == NON_EXISTENT_INT) DEFAULT_ICON_SIZE else it
        }

        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in 1..maxY) {
                var tileItemsIDs = IntArray(0)

                for (tileContent in initialDmmData.getTileContentByLocation(TileLocation.of(x, y))) {
                    if (dme.getItem(tileContent.type) != null) {
                        tileItemsIDs += TileItemProvider.getOrCreate(tileContent.type, tileContent.vars).id
                    }
                }

                tiles[y - 1][x - 1] = Tile(x, y, tileItemsIDs)
            }
        }
    }

    fun changeMapSize(newMaxX: Int, newMaxY: Int): List<Tile> {
        val deletedTiles = mutableListOf<Tile>()

        val baseTurfId = TileItemProvider.getOrCreate(Environment.dme.getBasicTurfType(), null).id
        val baseAreaId = TileItemProvider.getOrCreate(Environment.dme.getBasicAreaType(), null).id

        val tiles = Array(newMaxY) { arrayOfNulls<Tile>(newMaxX) }

        for (x in 1..newMaxX) {
            for (y in 1..newMaxY) {
                if (x > maxX || y > maxY) {
                    val tile = Tile(x, y, intArrayOf(baseAreaId, baseTurfId))
                    tiles[y - 1][x - 1] = tile
                    initialDmmData.addTile(x, y, tile.getTileContent())
                } else {
                    tiles[y - 1][x - 1] = getTile(x, y)
                }
            }
        }

        // Clean the initial dmm data and collect deleted tiles
        if (maxX > newMaxX || maxY > newMaxY) {
            for (x in 1..maxX) {
                for (y in 1..maxY) {
                    if (x > newMaxX || y > newMaxY) {
                        initialDmmData.deleteTile(x, y)
                        deletedTiles.add(getTile(x, y)!!)
                    }
                }
            }
        }

        initialDmmData.maxX = newMaxX
        initialDmmData.maxY = newMaxY
        maxX = newMaxX
        maxY = newMaxY
        this.tiles = tiles

        return deletedTiles
    }

    // if item from `placeTileItem` is not null then it means that we've placed replaceable type (turf or area)
    // to undo this action we need to place removed item back
    fun placeTileItemWithUndoable(x: Int, y: Int, tileItem: TileItem): Undoable {
        return getTile(x, y)?.placeTileItem(tileItem)?.let {
            PlaceTileItemAction(this, x, y, it.id)
        } ?: DeleteTileItemAction(this, x, y, tileItem.id)
    }

    fun placeTileItemWithUndoableByID(x: Int, y: Int, tileItemID: Int): Undoable {
        return placeTileItemWithUndoable(x, y, TileItemProvider.getByID(tileItemID))
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) tiles[y - 1][x - 1] else null

    fun getTileContentByLocation(location: TileLocation): TileContent {
        return getTile(location.x, location.y)!!.getTileContent()
    }

    fun getAllTileItemsByType(type: String): List<TileItem> {
        val items = mutableListOf<TileItem>()

        for (x in 1..maxX) {
            for (y in 1..maxY) {
                items.addAll(getTile(x, y)!!.getTileItemsByType(type))
            }
        }

        return items
    }

    fun getMaxX(): Int = maxX
    fun getMaxY(): Int = maxY

    override fun equals(other: Any?) = when {
        other == null -> false
        this === other -> true
        other !is Dmm -> false
        else -> mapPath == other.mapPath
    }

    override fun hashCode(): Int {
        return mapPath.hashCode()
    }
}
