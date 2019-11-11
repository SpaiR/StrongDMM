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

    init {
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
        area = tileItems.find { it.type.startsWith(TYPE_AREA) }
        turf = tileItems.find { it.type.startsWith(TYPE_TURF) }
        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.type.startsWith(TYPE_MOB) }
    }
}
