package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.logic.Environment

class Tile(val x: Int, val y: Int, private var tileItemsIDs: IntArray) {

    fun getTileItems(): List<TileItem> = TileItemProvider.getByIDs(tileItemsIDs)

    // The only place to use this method is a render loop, otherwise `::getTileItemsIDs()` should be used.
    fun unsafeTileItemsIDs(): IntArray = tileItemsIDs
    fun getTileItemsIDs(): IntArray = tileItemsIDs.copyOf()

    fun getTileItemsByType(type: String): List<TileItem> = getTileItems().filter { it.type == type }
    fun getAllTileItemsIsType(type: String): List<TileItem> = getTileItems().filter { it.isType(type) }

    fun getVisibleTileItems(): List<TileItem> = getTileItems().filter { !LayersManager.isHiddenType(it.type) }
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

    fun getHigherItemId(tileItemID: Int): Int {
        val index = tileItemsIDs.indexOf(tileItemID)

        // We are the one who is on the top
        if (index == tileItemsIDs.size - 1) {
            return NON_EXISTENT_INT
        }

        for (i in (index + 1) until tileItemsIDs.size) {
            val itemId = tileItemsIDs[i]
            val tileItem = TileItemProvider.getByID(itemId)
            if (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB)) {
                return itemId
            }
        }

        return NON_EXISTENT_INT
    }

    fun getLowerItemId(tileItemID: Int): Int {
        val index = tileItemsIDs.indexOf(tileItemID)

        // We are the one who is on the bottom
        if (index == 0) {
            return NON_EXISTENT_INT
        }

        for (i in (index - 1) downTo 0) {
            val itemId = tileItemsIDs[i]
            val tileItem = TileItemProvider.getByID(itemId)
            if (tileItem.isType(TYPE_OBJ) || tileItem.isType(TYPE_MOB)) {
                return itemId
            }
        }

        return NON_EXISTENT_INT
    }

    fun swapTileItem(which: Int, with: Int) {
        tileItemsIDs[tileItemsIDs.indexOf(which)] = with
    }

    fun switchTileItems(item1: Int, item2: Int) {
        val item1Index = tileItemsIDs.indexOf(item1)
        val item2Index = tileItemsIDs.indexOf(item2)
        val tmpArr = getTileItemsIDs()

        tmpArr[item1Index] = item2
        tmpArr[item2Index] = item1

        tileItemsIDs = tmpArr
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
            var deleted = false

            for (tileItemID in tileItemsIDs) {
                if (tileItemID != tileItem.id || deleted) {
                    tmpArr[counter++] = tileItemID
                } else {
                    deleted = true
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
        for (item in getVisibleTileItems().sortedWith(TileItemComparator).reversed()) {
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
}
