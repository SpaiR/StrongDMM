package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.*
import io.github.spair.strongdmm.logic.dme.Dme
import io.github.spair.strongdmm.logic.dme.TYPE_WORLD
import io.github.spair.strongdmm.logic.dme.VAR_ICON_SIZE
import io.github.spair.strongdmm.logic.history.DeleteTileItemAction
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.history.Undoable
import java.io.File

class Dmm(mapFile: File, val initialDmmData: DmmData, dme: Dme) {

    val mapName: String = mapFile.nameWithoutExtension
    val mapPath: String = mapFile.path

    val maxX: Int = initialDmmData.maxX
    val maxY: Int = initialDmmData.maxY
    val iconSize: Int = dme.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: 32

    private val tiles: Array<Array<Tile?>>

    init {
        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in 1..maxY) {
                val tileItems = mutableListOf<TileItem>()

                for (tileContent in initialDmmData.getTileContentByLocation(
                    TileLocation.of(
                        x,
                        y
                    )
                )) {
                    dme.getItem(tileContent.type)?.let {
                        tileItems.add(TileItem(it.type, tileContent.vars))
                    }
                }

                tiles[y - 1][x - 1] = Tile(x, y, tileItems)
            }
        }
    }

    // if item from `placeTileItem` is not null then it means that we've placed replaceable type (turf or area)
    // to undo this action we need to place removed item back
    fun placeTileItemWithUndoable(x: Int, y: Int, tileItem: TileItem): Undoable {
        return getTile(x, y)?.placeTileItem(tileItem)?.let {
            PlaceTileItemAction(this, x, y, it)
        } ?: DeleteTileItemAction(this, x, y, tileItem)
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) tiles[y - 1][x - 1] else null

    fun getTileContentByLocation(location: TileLocation): TileContent {
        val tileContent = TileContent()
        val tileObjects = mutableListOf<TileObject>()

        getTile(location.x, location.y)?.getTileItems()?.forEach { tileItem ->
            val tileObject = TileObject(tileItem.type)
            tileItem.customVars?.forEach { (k, v) -> tileObject.putVar(k, v) }
            tileObjects.add(tileObject)
        }

        // Consider to look at TileObjectComparator source if this line cause you a question
        tileObjects.sortedWith(TileObjectComparator()).forEach(tileContent::addTileObject)

        return tileContent
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