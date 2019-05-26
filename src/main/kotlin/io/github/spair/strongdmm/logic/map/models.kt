package io.github.spair.strongdmm.logic.map

import io.github.spair.dmm.io.*
import io.github.spair.strongdmm.gui.instancelist.ItemInstance
import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import io.github.spair.strongdmm.logic.dmi.SOUTH
import io.github.spair.strongdmm.logic.history.DeleteTileItemAction
import io.github.spair.strongdmm.logic.history.PlaceTileItemAction
import io.github.spair.strongdmm.logic.history.Undoable
import java.util.concurrent.CopyOnWriteArrayList

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

    // if item from `placeTileItem` is not null then it means that we've placed replaceable type (turf or area)
    // to undo this action we need to place removed item back
    fun placeTileItemWithUndoable(tileItem: TileItem): Undoable {
        return getTile(tileItem.x, tileItem.y)?.placeTileItem(tileItem)?.let {
            PlaceTileItemAction(this, it)
        } ?: DeleteTileItemAction(this, tileItem)
    }

    fun getTile(x: Int, y: Int) = if (x in 1..maxX && y in 1..maxY) tiles[y - 1][x - 1] else null

    fun getTileContentByLocation(location: TileLocation): TileContent {
        val tileContent = TileContent()
        val tileObjects = mutableListOf<TileObject>()

        getTile(location.x, location.y)?.getTileItems()?.forEach { tileItem ->
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

class Tile(val x: Int, val y: Int, tileItems: List<TileItem>) {

    private val tileItems: MutableList<TileItem> = CopyOnWriteArrayList(tileItems.sortedWith(TileObjectsComparator))

    // the only place to use this method is a render loop, otherwise `::getTileItems()` should be used
    fun getTileItemsUnsafe() = tileItems

    fun getTileItems() = tileItems.toList()
    fun getTileItemsByType(type: String) = tileItems.filter { it.type == type }

    fun getVisibleTileItems() = tileItems.filter { !LayersManager.isHiddenType(it.type) }

    fun addTileItem(tileItem: TileItem, sort: Boolean = true) {
        tileItem.x = x
        tileItem.y = y
        tileItems.add(tileItem)

        if (sort) {
            tileItems.sortWith(TileObjectsComparator)
        }
    }

    fun placeTileItem(tileItem: TileItem, sort: Boolean = true): TileItem? {
        // Specific BYOND behaviour: tile can have only one area or turf
        val typeToSanitize = when {
            tileItem.isType(TYPE_AREA) -> TYPE_AREA
            tileItem.isType(TYPE_TURF) -> TYPE_TURF
            else -> null
        }

        var removedItem: TileItem? = null

        if (typeToSanitize != null) {
            for (item in tileItems) {
                if (item.isType(typeToSanitize)) {
                    tileItems.remove(item)
                    removedItem = item
                    break
                }
            }
        }

        addTileItem(tileItem, sort)
        return removedItem
    }

    fun placeTileItems(tileItems: List<TileItem>) {
        tileItems.forEach { placeTileItem(it, false) }
        this.tileItems.sortWith(TileObjectsComparator)
    }

    fun replaceTileItems(tileItems: List<TileItem>) {
        clearTile()
        tileItems.forEach { placeTileItem(it, false) }
        this.tileItems.sortWith(TileObjectsComparator)
    }

    fun deleteTileItem(tileItem: TileItem) {
        tileItems.remove(tileItem)

        // Specific BYOND behaviour: tile always should have turf or area
        val varToGetItemType = when {
            tileItem.isType(TYPE_AREA) -> VAR_AREA
            tileItem.isType(TYPE_TURF) -> VAR_TURF
            else -> null
        }

        if (varToGetItemType != null) {
            val world = Environment.dme.getItem(TYPE_WORLD)!!
            val basicItem = Environment.dme.getItem(world.getVar(varToGetItemType)!!)!!
            addTileItem(TileItem(basicItem, tileItem.x, tileItem.y), false)
        }
    }

    fun clearTile() {
        getVisibleTileItems().forEach { deleteTileItem(it) }
    }

    fun findTopmostTileItem(typeToFind: String): TileItem? {
        for (item in tileItems.reversed()) {
            if (item.isType(typeToFind)) {
                return item
            }
        }
        return null
    }
}

class TileItem(val dmeItem: DmeItem, var x: Int, var y: Int, customVars: Map<String, String>? = null) {

    val customVars: MutableMap<String, String> = customVars?.toMutableMap() ?: mutableMapOf()

    companion object {
        fun fromInstance(instance: ItemInstance, x: Int, y: Int): TileItem {
            return TileItem(Environment.dme.getItem(instance.type)!!, x, y, instance.customVars)
        }
        fun fromTileItem(tileItem: TileItem, x: Int, y: Int): TileItem {
            return TileItem(tileItem.dmeItem, x, y, tileItem.customVars)
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

    fun reset() {
        customVars.clear()
        updateFields()
    }

    fun isType(type: String) = dmeItem.isType(type)

    fun getVar(name: String): String? = customVars[name] ?: dmeItem.getVar(name)
    fun getVarText(name: String): String? = customVars[name]?.takeIf { it.isNotEmpty() }?.run { substring(1, length - 1) } ?: dmeItem.getVarText(name)
    fun getVarInt(name: String): Int? = customVars[name]?.toIntOrNull() ?: dmeItem.getVarInt(name)
    fun getVarFloat(name: String): Float? = customVars[name]?.toFloatOrNull() ?: dmeItem.getVarFloat(name)
}

private object TileObjectsComparator : Comparator<TileItem> {
    override fun compare(o1: TileItem, o2: TileItem): Int {
        return if (o1.isType(TYPE_AREA)) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_OBJ)) 0
        else if (o1.isType(TYPE_OBJ) && (o2.isType(TYPE_MOB) || o2.isType(TYPE_TURF))) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_AREA)) 1
        else if (o1.isType(TYPE_MOB) && o2.isType(TYPE_TURF)) -1
        else 1
    }
}

data class CoordPoint(val x: Int, val y: Int)

data class CoordArea(val x1: Int, val y1: Int, val x2: Int, val y2: Int) {
    fun shiftToPoint(x: Int, y: Int) = CoordArea(x, y, x + x2 - x1, y + y2 - y1)
}
