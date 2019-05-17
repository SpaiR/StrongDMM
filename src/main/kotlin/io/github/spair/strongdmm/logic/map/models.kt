package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.*
import io.github.spair.strongdmm.gui.instancelist.ItemInstance
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.dmi.SOUTH

const val OUT_OF_BOUNDS = -1

class Dmm(val mapPath: String, val initialDmmData: DmmData, dme: Dme) {

    val maxX: Int = initialDmmData.maxX
    val maxY: Int = initialDmmData.maxY
    val iconSize: Int = dme.getItem(TYPE_WORLD)!!.getVarInt(VAR_ICON_SIZE) ?: 32

    private val tiles: Array<Array<Tile?>>

    init {
        tiles = Array(maxY) { arrayOfNulls<Tile>(maxX) }

        for (x in 1..maxX) {
            for (y in 1..maxY) {
                val tileItems = mutableListOf<TileItem>()

                for (tileContent in initialDmmData.getTileContentByLocation(TileLocation.of(x, y))) {
                    dme.getItem(tileContent.type)?.let {
                        tileItems.add(TileItem(it, x, y, tileContent.vars))
                    }
                }

                tiles[y - 1][x - 1] = Tile(x, y, tileItems)
            }
        }
    }

    // optional return item item is one of replaceable type (turf or area)
    fun placeTileItem(tileItem: TileItem): TileItem? {
        val tile = getTile(tileItem.x, tileItem.y)!!

        // Specific BYOND behaviour: tile can have only one area or turf
        val typeToSanitize = when {
            tileItem.isType(TYPE_AREA) -> TYPE_AREA
            tileItem.isType(TYPE_TURF) -> TYPE_TURF
            else -> null
        }

        var removedItem: TileItem? = null

        if (typeToSanitize != null) {
            for (item in tile) {
                if (item.isType(typeToSanitize)) {
                    tile.removeTileItem(item)
                    removedItem = item
                    break
                }
            }
        }

        tile.addTileItem(tileItem)
        return removedItem
    }

    fun deleteTileItem(tileItem: TileItem) {
        getTile(tileItem.x, tileItem.y)!!.removeTileItem(tileItem)

        // Specific BYOND behaviour: tile always should have turf or area
        val varToGetItemType = when {
            tileItem.isType(TYPE_AREA) -> VAR_AREA
            tileItem.isType(TYPE_TURF) -> VAR_TURF
            else -> null
        }

        if (varToGetItemType != null) {
            val world = Environment.dme.getItem(TYPE_WORLD)!!
            val basicItem = Environment.dme.getItem(world.getVar(varToGetItemType)!!)!!
            placeTileItem(TileItem(basicItem, tileItem.x, tileItem.y))
        }
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) tiles[y - 1][x - 1] else null

    fun getTileContentByLocation(location: TileLocation): TileContent {
        val tileContent = TileContent()
        val tileObjects = mutableListOf<TileObject>()

        getTile(location.x, location.y)?.forEach { tileItem ->
            val tileObject = TileObject(tileItem.type)
            tileItem.customVars.forEach { (k, v) -> tileObject.putVar(k, v) }
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
}

class Tile(val x: Int, val y: Int, private val tileItems: MutableList<TileItem>) : Iterable<TileItem> {

    fun getTileItems() = tileItems.toList()
    fun getTileItemsByType(type: String) = tileItems.filter { it.type == type }

    fun addTileItem(tileItem: TileItem) {
        tileItems.add(tileItem)
    }

    fun addTileItems(tileItems: Collection<TileItem>) {
        this.tileItems.addAll(tileItems)
    }

    fun removeTileItem(tileItem: TileItem) {
        tileItems.remove(tileItem)
    }

    fun clearTileItems() {
        tileItems.clear()
    }

    override fun iterator(): Iterator<TileItem> {
        return tileItems.iterator()
    }
}

class TileItem(val dmeItem: DmeItem, val x: Int, val y: Int, customVars: Map<String, String>? = null) {

    val customVars: MutableMap<String, String> = customVars?.toMutableMap() ?: mutableMapOf()

    companion object {
        fun fromInstance(instance: ItemInstance, x: Int, y: Int): TileItem {
            return TileItem(Environment.dme.getItem(instance.type)!!, x, y, instance.customVars)
        }
    }

    val type: String = dmeItem.type

    // Vars extensively used during rendering
    var icon = getVarText(VAR_ICON) ?: ""
    var iconState = getVarText(VAR_ICON_STATE) ?: ""
    var alpha = getVarInt(VAR_ALPHA) ?: 255
    var plane = getVarFloat(VAR_PLANE) ?: 0f
    var layer = getVarFloat(VAR_LAYER) ?: 0f
    var pixelX = getVarInt(VAR_PIXEL_X) ?: 0
    var pixelY = getVarInt(VAR_PIXEL_Y) ?: 0
    var dir = getVarInt(VAR_DIR) ?: SOUTH
    var color = getVarText(VAR_COLOR) ?: ""

    fun updateFields() {
        icon = getVarText(VAR_ICON) ?: ""
        iconState = getVarText(VAR_ICON_STATE) ?: ""
        alpha = getVarInt(VAR_ALPHA) ?: 255
        plane = getVarFloat(VAR_PLANE) ?: 0f
        layer = getVarFloat(VAR_LAYER) ?: 0f
        pixelX = getVarInt(VAR_PIXEL_X) ?: 0
        pixelY = getVarInt(VAR_PIXEL_Y) ?: 0
        dir = getVarInt(VAR_DIR) ?: SOUTH
        color = getVarText(VAR_COLOR) ?: ""
    }

    fun isType(type: String) = dmeItem.isType(type)

    fun getVar(name: String): String? = customVars[name] ?: dmeItem.getVar(name)
    fun getVarText(name: String): String? = customVars[name]?.takeIf { it.isNotEmpty() }?.run { substring(1, length - 1) } ?: dmeItem.getVarText(name)
    fun getVarInt(name: String): Int? = customVars[name]?.toIntOrNull() ?: dmeItem.getVarInt(name)
    fun getVarFloat(name: String): Float? = customVars[name]?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}
