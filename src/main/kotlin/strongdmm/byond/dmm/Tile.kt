package strongdmm.byond.dmm

import io.github.spair.dmm.io.TileContent
import io.github.spair.dmm.io.TileObject
import io.github.spair.dmm.io.TileObjectComparator
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF

/**
 * Helper class to do a very specific tile things like objects replacing or vars modifying.
 * Not meant to be used in maps themselves.
 */
class Tile(
    val dmm: Dmm,
    val x: Int,
    val y: Int
) {
    companion object {
        const val TILE_ITEM_IDX_AREA: Int = -1
        const val TILE_ITEM_IDX_TURF: Int = -2
    }

    lateinit var tileItems: List<TileItem>
        private set

    var area: TileItem? = null
        private set
    var turf: TileItem? = null
        private set
    lateinit var objs: List<IndexedValue<TileItem>>
        private set
    lateinit var mobs: List<IndexedValue<TileItem>>
        private set

    private var areaIndex: Int = TILE_ITEM_IDX_AREA
    private var turfIndex: Int = TILE_ITEM_IDX_TURF

    init {
        readObjectsFromMap()
    }

    fun getTileItemsId(): LongArray = dmm.getTileItemsId(x, y)

    fun getFilteredTileItems(filteredTypes: Collection<String>): List<TileItem> = tileItems.filter { !filteredTypes.contains(it.type) }

    fun addTileItem(tileItem: TileItem) {
        when {
            tileItem.isType(TYPE_AREA) -> {
                area?.let {
                    replaceTileItem(it.id, tileItem)
                }
            }
            tileItem.isType(TYPE_TURF) -> {
                turf?.let {
                    replaceTileItem(it.id, tileItem)
                }
            }
            else -> {
                dmm.setTileItemsId(x, y, getTileItemsId() + tileItem.id)
            }
        }
        readObjectsFromMap()
    }

    fun replaceTileItem(tileItemType: String, replaceWith: TileItem) {
        tileItems.findLast { it.type == tileItemType }?.let { tileItem ->
            replaceTileItem(tileItem, replaceWith)
        }
    }

    fun replaceTileItem(tileItemId: Long, replaceWith: TileItem) {
        tileItems.findLast { it.id == tileItemId }?.let { tileItem ->
            replaceTileItem(tileItem, replaceWith)
        }
    }

    fun replaceTileItem(tileItem: TileItem, replaceWith: TileItem) {
        getTileItemsId()[tileItems.indexOf(tileItem)] = replaceWith.id
        readObjectsFromMap()
    }

    fun deleteTileItem(tileItemType: String) {
        tileItems.findLast { it.type == tileItemType }?.let { tileItem ->
            deleteTileItem(tileItem)
        }
    }

    fun deleteTileItem(tileItemId: Long) {
        tileItems.findLast { it.id == tileItemId }?.let { tileItem ->
            deleteTileItem(tileItem)
        }
    }

    fun deleteTileItem(tileItem: TileItem) {
        when {
            tileItem.isType(TYPE_AREA) -> {
                replaceTileItem(tileItem, GlobalTileItemHolder.getOrCreate(dmm.basicAreaType))
            }
            tileItem.isType(TYPE_TURF) -> {
                replaceTileItem(tileItem, GlobalTileItemHolder.getOrCreate(dmm.basicTurfType))
            }
            else -> {
                val initialIds = getTileItemsId()
                val tileItemIdx = initialIds.lastIndexOf(tileItem.id)
                val newIds = LongArray(initialIds.size - 1)

                var idx = 0
                initialIds.forEachIndexed { index, id ->
                    if (index != tileItemIdx) {
                        newIds[idx++] = id
                    }
                }

                dmm.setTileItemsId(x, y, newIds)
            }
        }
        readObjectsFromMap()
    }

    fun replaceTileItemsId(tileItemsId: LongArray) {
        dmm.setTileItemsId(x, y, tileItemsId)
        readObjectsFromMap()
    }

    fun modifyItemVars(tileItemIdx: Int, vars: Map<String, String>?) {
        val itemIdx = when (tileItemIdx) {
            TILE_ITEM_IDX_AREA -> areaIndex
            TILE_ITEM_IDX_TURF -> turfIndex
            else -> tileItemIdx
        }

        getTileItemsId()[itemIdx] = GlobalTileItemHolder.getOrCreate(tileItems[itemIdx].type, vars).id
        readObjectsFromMap()
    }

    fun moveToTop(isMob: Boolean, tileItemIdx: Int) {
        shiftItem((if (isMob) mobs else objs), tileItemIdx, -1)
    }

    fun moveToBottom(isMob: Boolean, tileItemIdx: Int) {
        shiftItem((if (isMob) mobs else objs), tileItemIdx, 1)
    }

    fun getTileContent(): TileContent {
        val tileContent = TileContent()
        val tileObjects = mutableListOf<TileObject>()

        tileItems.forEach { tileItem ->
            val tileObject = TileObject(tileItem.type)
            tileItem.customVars?.forEach { (k, v) -> tileObject.putVar(k, v) }
            tileObjects.add(tileObject)
        }

        // Consider to look into the TileObjectComparator source if this line cause you a question
        tileObjects.sortedWith(TileObjectComparator()).forEach(tileContent::addTileObject)

        return tileContent
    }

    fun getTileItemIdx(tileItem: TileItem): Int {
        return tileItems.lastIndexOf(tileItem)
    }

    private fun shiftItem(list: List<IndexedValue<TileItem>>, tileItemIdx: Int, shiftValue: Int) {
        if (list.size == 1) {
            return
        }

        list.find { it.index == tileItemIdx }?.let {
            val relativeIdx = list.indexOf(it)
            val swapWithIdx = relativeIdx + shiftValue
            if (swapWithIdx >= 0 && swapWithIdx < list.size) {
                val swapWithItem = list[swapWithIdx]
                val tileItemsId = getTileItemsId()
                tileItemsId[swapWithItem.index] = it.value.id
                tileItemsId[it.index] = swapWithItem.value.id
            }
        }

        readObjectsFromMap()
    }

    private fun readObjectsFromMap() {
        // List with all tile items
        tileItems = getTileItemsId().map { GlobalTileItemHolder.getById(it) }

        // Find area and its index in tile items list
        tileItems.withIndex().find { it.value.isType(TYPE_AREA) }.let {
            area = it?.value
            areaIndex = it?.index ?: TILE_ITEM_IDX_AREA
        }

        // Find turf and its index in tile items list
        tileItems.withIndex().find { it.value.isType(TYPE_TURF) }.let {
            turf = it?.value
            turfIndex = it?.index ?: TILE_ITEM_IDX_TURF
        }

        // Collect indexed objects and mobs
        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.isType(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.isType(TYPE_MOB) }
    }
}
