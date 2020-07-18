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
    private val tileItemsIdByType: MutableMap<String, TLongList> = mutableMapOf()

    fun resetEnvironment() {
        tileItems.clear()
        tileItemsIdByType.clear()
        tmpTileItems.clear()
    }

    inline fun tmpOperation(action: () -> Unit) {
        isTmpMode = true
        action()
        isTmpMode = false
    }

    fun getOrCreate(type: String, vars: Map<String, String>? = null): TileItem {
        val typeVarsConcat = getTypeVarsConcat(type, vars)
        val hash = djb2hash(typeVarsConcat)
        return getTileItemIfExists(hash) ?: createTileItem(hash, type, vars)
    }

    fun getById(id: Long): TileItem = tileItems[id] ?: tmpTileItems[id]

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

    private fun getTileItemIfExists(hash: Long): TileItem? {
        return if (isTmpMode) {
            tmpTileItems.get(hash)
        } else {
            tileItems.get(hash)
        }
    }

    private fun createTileItem(hash: Long, type: String, vars: Map<String, String>?): TileItem {
        val tileItem = TileItem(hash, environment.getItem(type)!!, vars)

        if (!isTmpMode) {
            tileItems.put(hash, tileItem)
            tileItemsIdByType.getOrPut(type) { TLongArrayList() }.add(hash)
        } else {
            tmpTileItems.put(hash, tileItem)
        }

        return tileItem
    }
}
