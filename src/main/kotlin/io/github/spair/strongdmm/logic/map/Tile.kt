package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*
import java.util.concurrent.CopyOnWriteArrayList

class Tile(val x: Int, val y: Int, tileItemsIDs: List<Int>) {

    private val tileItemsIDs: MutableList<Int> = CopyOnWriteArrayList(tileItemsIDs)

    val tileItems: List<TileItem>
        get() = TileItemProvider.getByIDs(tileItemsIDs)

    // the only place to use this method is a render loop, otherwise `::getTileItemsIDs()` should be used
    fun unsafeGetTileItemsIDs(): List<Int> = tileItemsIDs

    fun getTileItemsIDs(): List<Int> = tileItemsIDs.toList()
    fun getTileItemsByType(type: String): List<TileItem> = tileItems.filter { it.type == type }

    fun getVisibleTileItems(): List<TileItem> = tileItems.filter { !LayersManager.isHiddenType(it.type) }
    fun getVisibleTileItemsIDs(): List<Int> = getVisibleTileItems().map { it.id }

    fun placeTileItem(tileItem: TileItem): TileItem? {
        // Specific BYOND behaviour: tile can have only one area or turf
        val typeToSanitize = when {
            tileItem.isType(TYPE_AREA) -> TYPE_AREA
            tileItem.isType(TYPE_TURF) -> TYPE_TURF
            else -> null
        }

        var removedItem: TileItem? = null

        if (typeToSanitize != null) {
            for (id in tileItemsIDs) {
                val item = TileItemProvider.getByID(id)
                if (item.isType(typeToSanitize)) {
                    tileItemsIDs.remove(id)
                    removedItem = item
                    break
                }
            }
        }

        tileItemsIDs.add(tileItem.id)
        return removedItem
    }

    fun swapTileItem(which: Int, with: Int) {
        tileItemsIDs.remove(which)
        tileItemsIDs.add(with)
    }

    fun fullReplaceTileItemsByIDs(tileItemsIDs: List<Int>) {
        with(this.tileItemsIDs) {
            clear()
            addAll(tileItemsIDs)
        }
    }

    fun replaceOnlyVisibleTileItemsByIDs(tileItemsIDs: List<Int>) {
        deleteVisibleTileItems()
        tileItemsIDs.forEach { placeTileItem(TileItemProvider.getByID(it)) }
    }

    fun deleteTileItem(tileItem: TileItem) {
        tileItemsIDs.remove(tileItem.id)

        // Specific BYOND behaviour: tile always should have turf or area
        val varToGetItemType = when {
            tileItem.isType(TYPE_AREA) -> VAR_AREA
            tileItem.isType(TYPE_TURF) -> VAR_TURF
            else -> null
        }

        if (varToGetItemType != null) {
            val world = Environment.dme.getItem(TYPE_WORLD)!!
            val basicItem = Environment.dme.getItem(world.getVar(varToGetItemType)!!)!!
            tileItemsIDs.add(TileItemProvider.getOrCreate(basicItem.type, null).id)
        }
    }

    fun deleteTileItemByID(tileItemID: Int) {
        deleteTileItem(TileItemProvider.getByID(tileItemID))
    }

    fun deleteVisibleTileItems() {
        getVisibleTileItems().forEach { deleteTileItem(it) }
    }

    fun findTopmostTileItem(typeToFind: String): TileItem? {
        for (item in tileItems.sortedWith(TileItemsComparator).reversed()) {
            if (item.isType(typeToFind)) {
                return item
            }
        }
        return null
    }

    // Will replace tile item with the new on, which will have new vars
    fun setTileItemVars(tileItem: TileItem, newVars: Map<String, String>?): TileItem {
        val newTileItem = TileItemProvider.getOrCreate(tileItem.type, newVars)
        tileItemsIDs.remove(tileItem.id)
        tileItemsIDs.add(newTileItem.id)
        return newTileItem
    }

    // Will replace tile item with the new on, which will have new vars
    fun addTileItemVars(tileItem: TileItem, vars: Map<String, String>?): TileItem {
        val newVars = if (vars != null) {
            tileItem.customVars?.toMutableMap()?.apply { putAll(vars) } ?: vars
        } else {
            null
        }

        val newTileItem = TileItemProvider.getOrCreate(tileItem.type, newVars)
        tileItemsIDs.remove(tileItem.id)
        tileItemsIDs.add(newTileItem.id)
        return newTileItem
    }

    // Will replace tile item with the new on, which will have new vars
    fun removeTileItemVar(tileItem: TileItem, varName: String): TileItem {
        val newVars = tileItem.customVars?.toMutableMap()?.apply { remove(varName) }
        val newTileItem = TileItemProvider.getOrCreate(tileItem.type, newVars)
        tileItemsIDs.remove(tileItem.id)
        tileItemsIDs.add(newTileItem.id)
        return newTileItem
    }
}

// area -> obj -> mob -> turf
object TileItemsComparator : Comparator<TileItem> {
    override fun compare(o1: TileItem, o2: TileItem): Int {
        return if (o1.isType(TYPE_AREA)) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_OBJ)) 0
        else if (o1.isType(TYPE_OBJ) && (o2.isType(TYPE_MOB) || o2.isType(TYPE_TURF))) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_AREA)) 1
        else if (o1.isType(TYPE_MOB) && o2.isType(TYPE_TURF)) -1
        else 1
    }
}
