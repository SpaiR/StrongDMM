package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.logic.Environment
import io.github.spair.strongdmm.logic.dme.*

class Tile(val x: Int, val y: Int, private var tileItemsIDs: IntArray) {

    val tileItems: List<TileItem>
        get() = TileItemProvider.getByIDs(tileItemsIDs)

    // The only place to use this method is a render loop, otherwise `::getTileItemsIDs()` should be used.
    fun unsafeTileItemsIDs(): IntArray = tileItemsIDs

    fun getTileItemsIDs(): IntArray = tileItemsIDs.copyOf()
    fun getTileItemsByType(type: String): List<TileItem> = tileItems.filter { it.type == type }

    fun getVisibleTileItems(): List<TileItem> = tileItems.filter { !LayersManager.isHiddenType(it.type) }
    fun getVisibleTileItemsIDs(): IntArray = getVisibleTileItems().map { it.id }.toIntArray()

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
                    removedItem = item
                    break
                }
            }
        }

        if (removedItem != null) {
            swapTileItem(removedItem.id, tileItem.id)
        } else {
            tileItemsIDs += tileItem.id
        }

        return removedItem
    }

    fun swapTileItem(which: Int, with: Int) {
        tileItemsIDs[tileItemsIDs.indexOf(which)] = with
    }

    fun fullReplaceTileItemsByIDs(tileItemsIDs: IntArray) {
        this.tileItemsIDs = tileItemsIDs.copyOf()
    }

    fun replaceOnlyVisibleTileItemsByIDs(tileItemsIDs: IntArray) {
        deleteVisibleTileItems()
        tileItemsIDs.forEach { placeTileItem(TileItemProvider.getByID(it)) }
    }

    fun deleteTileItem(tileItem: TileItem) {
        // Specific BYOND behaviour: tile always should have turf or area
        val varToGetItemType = when {
            tileItem.isType(TYPE_AREA) -> VAR_AREA
            tileItem.isType(TYPE_TURF) -> VAR_TURF
            else -> null
        }

        var newTileItem: TileItem? = null

        if (varToGetItemType != null) {
            val world = Environment.dme.getItem(TYPE_WORLD)!!
            val basicItem = Environment.dme.getItem(world.getVar(varToGetItemType)!!)!!
            newTileItem = TileItemProvider.getOrCreate(basicItem.type, null)
        }

        if (newTileItem != null) {
            swapTileItem(tileItem.id, newTileItem.id)
        } else {
            val tmpArr = IntArray(tileItemsIDs.size - 1)
            var counter = 0

            tileItemsIDs.forEach { tileItemID ->
                if (tileItemID != tileItem.id) {
                    tmpArr[counter++] = tileItemID
                }
            }

            tileItemsIDs = tmpArr
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
        swapTileItem(tileItem.id, newTileItem.id)
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
        swapTileItem(tileItem.id, newTileItem.id)
        return newTileItem
    }

    // Will replace tile item with the new on, which will have new vars
    fun removeTileItemVar(tileItem: TileItem, varName: String): TileItem {
        val newVars = tileItem.customVars?.toMutableMap()?.apply { remove(varName) }
        val newTileItem = TileItemProvider.getOrCreate(tileItem.type, newVars)
        swapTileItem(tileItem.id, newTileItem.id)
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
