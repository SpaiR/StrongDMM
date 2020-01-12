package strongdmm.byond.dmm

import gnu.trove.list.TLongList
import gnu.trove.list.array.TLongArrayList
import gnu.trove.map.hash.TLongObjectHashMap
import strongdmm.byond.dme.Dme

object GlobalTileItemHolder {
    lateinit var environment: Dme

    private val tileItems: TLongObjectHashMap<TileItem> = TLongObjectHashMap()
    private val tileItemsIdByType: MutableMap<String, TLongList> = mutableMapOf()

    fun resetEnvironment() {
        tileItems.clear()
        tileItemsIdByType.clear()
    }

    fun getOrCreate(type: String, vars: Map<String, String>?): TileItem {
        var typeVarsConcat = type

        // Concatenate type with available variables to get unique representation of the tile item
        if (vars != null && vars.isNotEmpty()) {
            typeVarsConcat += buildString {
                vars.toSortedMap().forEach { (value, key) ->
                    append(value).append(key)
                }
            }
        }

        // djb2 algorithm http://www.cse.yorku.ca/~oz/hash.html
        var hash = 5381L
        typeVarsConcat.forEach { c ->
            hash = ((hash shl 5) + hash) + c.toInt()
        }

        if (tileItems.contains(hash)) {
            return tileItems.get(hash)
        }

        val tileItem = TileItem(hash, environment.getItem(type)!!, vars)
        tileItems.put(hash, tileItem)
        tileItemsIdByType.getOrPut(type) { TLongArrayList() }.add(hash)
        return tileItem
    }

    fun getById(id: Long): TileItem = tileItems[id]

    fun getTileItemsByType(type: String): List<TileItem> {
        val tileItems = mutableListOf<TileItem>()
        getOrCreate(type, null) // Ensure that default tile item exists
        tileItemsIdByType[type]?.forEach { tileItems.add(getById(it)) }
        return tileItems
    }
}
