package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import java.util.concurrent.CopyOnWriteArrayList

class Tile(val x: Int, val y: Int, tileItems: List<TileItem>) {

    private val tileItems: MutableList<TileItem> =
        CopyOnWriteArrayList(tileItems.sortedWith(TileObjectsComparator))

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

    fun replaceVisibleTileItems(tileItems: List<TileItem>) {
        clearVisibleTile()
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
            addTileItem(TileItem(basicItem.type, tileItem.x, tileItem.y), false)
        }
    }

    fun clearTile() {
        getTileItems().forEach { deleteTileItem(it) }
    }

    fun clearVisibleTile() {
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
