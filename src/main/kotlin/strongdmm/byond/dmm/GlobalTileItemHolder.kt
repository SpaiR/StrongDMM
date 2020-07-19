package strongdmm.byond.dmm

import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import gnu.trove.map.hash.TLongObjectHashMap
import strongdmm.byond.dme.Dme

object GlobalTileItemHolder {
    lateinit var environment: Dme

    var isTmpMode: Boolean = false

    private val tileItems: TLongObjectHashMap<TileItem> = TLongObjectHashMap()
    private val tmpTileItems: TLongObjectHashMap<TileItem> = TLongObjectHashMap()
    private val removedTileItems: TLongObjectHashMap<TileItem> = TLongObjectHashMap()
    private val tileItemsIdByType: MutableMap<String, TLongList> = mutableMapOf()

    fun resetEnvironment() {
        tileItems.clear()
        tileItemsIdByType.clear()
        tmpTileItems.clear()
        removedTileItems.clear()
    }

    inline fun tmpOperation(action: () -> Unit) {
        isTmpMode = true
        action()
        isTmpMode = false
    }

    fun remove(tileItem: TileItem) {
        tileItemsIdByType[tileItem.type]?.remove(tileItem.id)
        removedTileItems.put(tileItem.id, tileItems.remove(tileItem.id))
    }

    fun getOrCreate(type: String, vars: Map<String, String>? = null): TileItem {
        val typeVarsConcat = getTypeVarsConcat(type, vars)
        val id = djb2hash(typeVarsConcat)
        return getTileItemIfExists(id) ?: createTileItem(id, type, vars)
    }

    fun getById(id: Long): TileItem {
        tryRestoreTileItem(id)
        return tileItems[id] ?: tmpTileItems[id]
    }

    fun getTileItemsByType(type: String): List<TileItem> {
        val tileItems = mutableListOf<TileItem>()
        getOrCreate(type) // Ensure that default tile item exists
        tileItemsIdByType[type]?.forEach { tileItems.add(getById(it)) }
        return tileItems
    }

    // Concatenate type with available variables to get unique representation of the tile item
    private fun getTypeVarsConcat(type: String, vars: Map<String, String>? = null): String {
        var typeVarsConcat = type

        if (vars != null && vars.isNotEmpty()) {
            typeVarsConcat += buildString {
                vars.toSortedMap().forEach { (value, key) ->
                    append(value).append(key)
                }
            }
        }

        return typeVarsConcat
    }

    // djb2 algorithm http://www.cse.yorku.ca/~oz/hash.html
    private fun djb2hash(str: String): Long {
        var hash = 5381L

        str.forEach { char ->
            hash = ((hash shl 5) + hash) + char.toInt()
        }

        return hash
    }

    fun tryRestoreTileItem(id: Long) {
        if (removedTileItems.contains(id)) {
            storeTileItem(removedTileItems.remove(id))
        }
    }

    private fun getTileItemIfExists(id: Long): TileItem? {
        tryRestoreTileItem(id)

        return if (isTmpMode) {
            tmpTileItems.get(id)
        } else {
            tileItems.get(id)
        }
    }

    private fun createTileItem(id: Long, type: String, vars: Map<String, String>?): TileItem {
        val tileItem = TileItem(id, environment.getItem(type)!!, vars)
        storeTileItem(tileItem)
        return tileItem
    }

    private fun storeTileItem(tileItem: TileItem) {
        if (!isTmpMode) {
            tileItems.put(tileItem.id, tileItem)
            tileItemsIdByType.getOrPut(tileItem.type) { TLongArrayList() }.add(tileItem.id)
        } else {
            tmpTileItems.put(tileItem.id, tileItem)
        }
    }
}
