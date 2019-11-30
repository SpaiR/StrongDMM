package strongdmm.byond.dmm

import io.github.spair.dmm.io.TileContent
import io.github.spair.dmm.io.TileObject
import io.github.spair.dmm.io.TileObjectComparator
import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF

class Tile(
    private val dmm: Dmm,
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
        update()
    }

    fun getTileItemsId(): IntArray = dmm.getTileItemsId(x, y)

    fun replaceTileItemsId(tileItemsId: IntArray) = dmm.setTileItemsId(x, y, tileItemsId)

    fun modifyItemVars(tileItemIdx: TileItemIdx, vars: Map<String, String>?) {
        val itemIdx = when (tileItemIdx) {
            TileItemIdx.AREA -> areaIndex
            TileItemIdx.TURF -> turfIndex
            else -> tileItemIdx
        }

        val tileItemsId = dmm.getTileItemsId(x, y)
        val tileItemType = tileItems[itemIdx.value].type

        tileItemsId[itemIdx.value] = GlobalTileItemHolder.getOrCreate(tileItemType, vars).id
        update()
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

        // Consider to look at TileObjectComparator source if this line cause you a question
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

        update()
    }

    private fun update() {
        tileItems = dmm.getTileItemsId(x, y).map { GlobalTileItemHolder.getById(it) }.toMutableList()

        tileItems.withIndex().find { it.value.type.startsWith(TYPE_AREA) }.let {
            area = it?.value
            areaIndex = it?.index?.let { idx -> TileItemIdx(idx) } ?: TileItemIdx.AREA
        }

        tileItems.withIndex().find { it.value.type.startsWith(TYPE_TURF) }.let {
            turf = it?.value
            turfIndex = it?.index?.let { idx -> TileItemIdx(idx) } ?: TileItemIdx.TURF
        }

        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_MOB) }
    }
}
