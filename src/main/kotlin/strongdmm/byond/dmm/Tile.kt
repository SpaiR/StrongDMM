package strongdmm.byond.dmm

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_MOB
import strongdmm.byond.TYPE_OBJ
import strongdmm.byond.TYPE_TURF

class Tile(
    private val dmm: Dmm,
    val x: Int,
    val y: Int
) {
    companion object {
        const val AREA_INDEX: Int = -1
        const val TURF_INDEX: Int = -2
    }

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

    private var areaIndex: Int = AREA_INDEX
    private var turfIndex: Int = TURF_INDEX

    init {
        update()
    }

    fun modifyItemVars(tileItemIdx: Int, vars: Map<String, String>?) {
        val itemIdx = when (tileItemIdx) {
            AREA_INDEX -> areaIndex
            TURF_INDEX -> turfIndex
            else -> tileItemIdx
        }

        val tileItemsID = dmm.getTileItemsID(x, y)
        val tileItemType = tileItems[itemIdx].type

        tileItemsID[itemIdx] = GlobalTileItemHolder.getOrCreate(tileItemType, vars).id
        update()
    }

    fun moveToTop(isMob: Boolean, index: Int) {
        shiftItem((if (isMob) mobs else objs), index, -1)
    }

    fun moveToBottom(isMob: Boolean, index: Int) {
        shiftItem((if (isMob) mobs else objs), index, 1)
    }

    private fun shiftItem(list: List<IndexedValue<TileItem>>, index: Int, shiftValue: Int) {
        if (list.size == 1) {
            return
        }

        list.find { it.index == index }?.let {
            val relativeIdx = list.indexOf(it)
            val swapWithIdx = relativeIdx + shiftValue
            if (swapWithIdx >= 0 && swapWithIdx < list.size) {
                val swapWithItem = list[swapWithIdx]
                val tileItemsID = dmm.getTileItemsID(x, y)
                tileItemsID[swapWithItem.index] = it.value.id
                tileItemsID[it.index] = swapWithItem.value.id
            }
        }

        update()
    }

    private fun update() {
        tileItems = dmm.getTileItemsID(x, y).map { GlobalTileItemHolder.getById(it) }.toMutableList()

        tileItems.withIndex().find { it.value.type.startsWith(TYPE_AREA) }.let {
            area = it?.value
            areaIndex = it?.index ?: AREA_INDEX
        }

        tileItems.withIndex().find { it.value.type.startsWith(TYPE_TURF) }.let {
            turf = it?.value
            turfIndex = it?.index ?: TURF_INDEX
        }

        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_MOB) }
    }
}
