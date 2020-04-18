package strongdmm.byond.dmm

import strongdmm.byond.*
import strongdmm.byond.dmm.parser.TileContent
import strongdmm.byond.dmm.parser.TileObject
import strongdmm.byond.dmm.parser.TileObjectComparator
import strongdmm.controller.preferences.NudgeMode

/**
 * Helper class to do a very specific tile things like objects replacing or vars modifying.
 * Not meant to be used in maps themselves.
 */
class Tile(
    val dmm: Dmm,
    val x: Int,
    val y: Int,
    val z: Int
) {
    lateinit var tileItems: List<TileItem>
        private set

    var area: IndexedValue<TileItem>? = null
        private set
    var turf: IndexedValue<TileItem>? = null
        private set
    lateinit var objs: List<IndexedValue<TileItem>>
        private set
    lateinit var mobs: List<IndexedValue<TileItem>>
        private set

    init {
        readObjectsFromMap()
    }

    fun getTileItemsId(): LongArray = dmm.getTileItemsId(x, y, z)

    fun getFilteredTileItems(filteredTypes: Collection<String>): List<TileItem> = tileItems.filter { !filteredTypes.contains(it.type) }

    fun addTileItem(tileItem: TileItem) {
        when {
            tileItem.isType(TYPE_AREA) -> {
                area?.let {
                    replaceTileItem(it.value.id, tileItem)
                }
            }
            tileItem.isType(TYPE_TURF) -> {
                turf?.let {
                    replaceTileItem(it.value.id, tileItem)
                }
            }
            else -> {
                dmm.setTileItemsId(x, y, z, getTileItemsId() + tileItem.id)
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
        replaceTileItem(tileItems.lastIndexOf(tileItem), replaceWith)
    }

    fun replaceTileItem(tileItemIdx: Int, replaceWith: TileItem) {
        if (tileItems[tileItemIdx].isSameType(replaceWith)) {
            setTileItemsIdWithReplace(tileItemIdx, replaceWith.id)
            readObjectsFromMap()
        }
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
        deleteTileItem(tileItems.lastIndexOf(tileItem))
    }

    fun deleteTileItem(tileItemIdx: Int) {
        val tileItem = tileItems[tileItemIdx]

        when {
            tileItem.isType(TYPE_AREA) -> {
                replaceTileItem(tileItem, GlobalTileItemHolder.getOrCreate(dmm.basicAreaType))
            }
            tileItem.isType(TYPE_TURF) -> {
                replaceTileItem(tileItem, GlobalTileItemHolder.getOrCreate(dmm.basicTurfType))
            }
            else -> {
                val initialIds = getTileItemsId()
                val newIds = LongArray(initialIds.size - 1)

                var idx = 0
                initialIds.forEachIndexed { index, id ->
                    if (index != tileItemIdx) {
                        newIds[idx++] = id
                    }
                }

                dmm.setTileItemsId(x, y, z, newIds)
            }
        }

        readObjectsFromMap()
    }

    fun replaceTileItemsId(tileItemsId: LongArray) {
        dmm.setTileItemsId(x, y, z, tileItemsId)
        readObjectsFromMap()
    }

    fun modifyItemVars(tileItemIdx: Int, vars: Map<String, String>?) {
        var newVars: MutableMap<String, String>? = null

        // internal vars sanitizing to prevent problems BYOND does
        if (vars != null && vars.isNotEmpty()) {
            val tileItem = tileItems[tileItemIdx]
            newVars = vars.toMutableMap()

            vars.forEach { (key, value) ->
                if (tileItem.dmeItem.getVar(key) == value) {
                    newVars.remove(key)
                }
            }
        }

        setTileItemsIdWithReplace(tileItemIdx, GlobalTileItemHolder.getOrCreate(tileItems[tileItemIdx].type, newVars).id)
        readObjectsFromMap()
    }

    fun nudge(isXAxis: Boolean, tileItem: TileItem, tileItemIdx: Int, value: Int, nudgeMode: NudgeMode) {
        val vars = tileItem.customVars?.toMutableMap() ?: mutableMapOf()

        val axis = when (nudgeMode) {
            NudgeMode.PIXEL -> {
                if (isXAxis) VAR_PIXEL_X else VAR_PIXEL_Y
            }
            NudgeMode.STEP -> {
                if (isXAxis) VAR_STEP_X else VAR_STEP_Y
            }
        }

        vars[axis] = value.toString()
        modifyItemVars(tileItemIdx, vars)
    }

    fun setDir(tileItem: TileItem, tileItemIdx: Int, dir: Int) {
        val vars = tileItem.customVars?.toMutableMap() ?: mutableMapOf()
        vars[VAR_DIR] = dir.toString()
        modifyItemVars(tileItemIdx, vars)
    }

    fun moveToTop(tileItem: TileItem, tileItemIdx: Int) {
        shiftItem((if (tileItem.isType(TYPE_MOB)) mobs else objs), tileItemIdx, -1)
    }

    fun moveToBottom(tileItem: TileItem, tileItemIdx: Int) {
        shiftItem((if (tileItem.isType(TYPE_MOB)) mobs else objs), tileItemIdx, 1)
    }

    fun getTileContent(): TileContent {
        val tileContent = TileContent()
        val tileObjects = mutableListOf<TileObject>()

        tileItems.forEach { tileItem ->
            val tileObject = TileObject(tileItem.type)
            tileObject.setVars(tileItem.customVars)
            tileObjects.add(tileObject)
        }

        // Consider to look into the TileObjectComparator source if this line cause you a question
        tileContent.content.addAll(tileObjects.sortedWith(TileObjectComparator()))

        return tileContent
    }

    fun getTileItemIdx(tileItem: TileItem): Int {
        return tileItems.lastIndexOf(tileItem)
    }

    private fun setTileItemsIdWithReplace(tileItemIdx: Int, replaceWithId: Long) {
        val tileItemsId = getTileItemsId().copyOf()
        tileItemsId[tileItemIdx] = replaceWithId
        dmm.setTileItemsId(x, y, z, tileItemsId)
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
                val tileItemsId = getTileItemsId().copyOf()
                tileItemsId[swapWithItem.index] = it.value.id
                tileItemsId[it.index] = swapWithItem.value.id
                dmm.setTileItemsId(x, y, z, tileItemsId)
            }
        }

        readObjectsFromMap()
    }

    private fun readObjectsFromMap() {
        // List with all tile items
        tileItems = getTileItemsId().map { GlobalTileItemHolder.getById(it) }

        // Find area and its index in tile items list
        tileItems.withIndex().find { it.value.isType(TYPE_AREA) }.let {
            area = it
        }

        // Find turf and its index in tile items list
        tileItems.withIndex().find { it.value.isType(TYPE_TURF) }.let {
            turf = it
        }

        // Collect indexed objects and mobs
        objs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.isType(TYPE_OBJ) }
        mobs = tileItems.withIndex().reversed().filter { (_, tileItem) -> tileItem.isType(TYPE_MOB) }
    }
}
