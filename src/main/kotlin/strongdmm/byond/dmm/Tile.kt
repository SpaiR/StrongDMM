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
 * No meant to be used in maps themselves.
 */
class Tile(
    val dmm: Dmm,
    val x: Int,
    val y: Int
) {
    lateinit var tileItems: MutableList<TileItem>
        private set
    var area: TileItem? = null
        private set
    var turf: TileItem? = null
        private set
    lateinit var objs: List<IndexedValue<TileItem>>
        private set
    lateinit var mobs: List<IndexedValue<TileItem>>
        private set

    private var areaIndex: TileItemIdx = TileItemIdx.AREA
    private var turfIndex: TileItemIdx = TileItemIdx.TURF

    init {
        readObjectsFromMap()
    }

    fun getTileItemsId(): LongArray = dmm.getTileItemsId(x, y)

    fun replaceTileItem(tileItemType: String, replaceWith: TileItem) {
        tileItems.find { it.type == tileItemType }?.let { tileItem ->
            dmm.getTileItemsId(x, y)[tileItems.indexOf(tileItem)] = replaceWith.id
        }
    }

    fun replaceTileItem(tileItemId: Long, replaceWith: TileItem) {
        tileItems.find { it.id == tileItemId }?.let { tileItem ->
            dmm.getTileItemsId(x, y)[tileItems.indexOf(tileItem)] = replaceWith.id
        }
    }

    fun deleteTileItem(tileItemType: String) {
        tileItems.find { it.type == tileItemType }?.let { tileItem ->
            tileItems.remove(tileItem)
            dmm.setTileItemsId(x, y, tileItems.asSequence().map { it.id }.toList().toLongArray())
        }
    }

    fun deleteTileItem(tileItemId: Long) {
        tileItems.find { it.id == tileItemId }?.let { tileItem ->
            tileItems.remove(tileItem)
            dmm.setTileItemsId(x, y, tileItems.asSequence().map { it.id }.toList().toLongArray())
        }
    }

    fun replaceTileItemsId(tileItemsId: LongArray) = dmm.setTileItemsId(x, y, tileItemsId)

    fun modifyItemVars(tileItemIdx: TileItemIdx, vars: Map<String, String>?) {
        val itemIdx = when (tileItemIdx) {
            TileItemIdx.AREA -> areaIndex
            TileItemIdx.TURF -> turfIndex
            else -> tileItemIdx
        }

        val tileItemsId = dmm.getTileItemsId(x, y)
        val tileItemType = tileItems[itemIdx.value].type

        tileItemsId[itemIdx.value] = GlobalTileItemHolder.getOrCreate(tileItemType, vars).id
        readObjectsFromMap()
    }

    fun moveToTop(isMob: Boolean, index: TileItemIdx) {
        shiftItem((if (isMob) mobs else objs), index, -1)
    }

    fun moveToBottom(isMob: Boolean, index: TileItemIdx) {
        shiftItem((if (isMob) mobs else objs), index, 1)
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

    private fun shiftItem(list: List<IndexedValue<TileItem>>, index: TileItemIdx, shiftValue: Int) {
        if (list.size == 1) {
            return
        }

        list.find { it.index == index.value }?.let {
            val relativeIdx = list.indexOf(it)
            val swapWithIdx = relativeIdx + shiftValue
            if (swapWithIdx >= 0 && swapWithIdx < list.size) {
                val swapWithItem = list[swapWithIdx]
                val tileItemsId = dmm.getTileItemsId(x, y)
                tileItemsId[swapWithItem.index] = it.value.id
                tileItemsId[it.index] = swapWithItem.value.id
            }
        }

        readObjectsFromMap()
    }

    private fun readObjectsFromMap() {
        // List with all tile items
        tileItems = dmm.getTileItemsId(x, y).map { GlobalTileItemHolder.getById(it) }.toMutableList()

        // Find area and its index in tile items list
        tileItems.withIndex().find { it.value.type.startsWith(TYPE_AREA) }.let {
            area = it?.value
            areaIndex = it?.index?.let { idx -> TileItemIdx(idx) } ?: TileItemIdx.AREA
        }

        // Find turf and its index in tile items list
        tileItems.withIndex().find { it.value.type.startsWith(TYPE_TURF) }.let {
            turf = it?.value
            turfIndex = it?.index?.let { idx -> TileItemIdx(idx) } ?: TileItemIdx.TURF
        }

        // Collect indexed objects and mobs
        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_MOB) }
    }
}
