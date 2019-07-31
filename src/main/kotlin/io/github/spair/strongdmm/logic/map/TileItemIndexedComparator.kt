package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.common.TYPE_AREA
import io.github.spair.strongdmm.common.TYPE_MOB
import io.github.spair.strongdmm.common.TYPE_OBJ
import io.github.spair.strongdmm.common.TYPE_TURF

// area -> obj -> mob -> turf
object TileItemIndexedComparator : Comparator<IndexedValue<TileItem>> {
    override fun compare(oi1: IndexedValue<TileItem>, oi2: IndexedValue<TileItem>): Int {
        val o1 = oi1.value
        val o2 = oi2.value
        return if (o1.isType(TYPE_AREA)) {
            -1
        } else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_OBJ)) {
            0
        } else if (o1.isType(TYPE_OBJ) && (o2.isType(TYPE_MOB) || o2.isType(TYPE_TURF))) {
            -1
        } else {
            if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_AREA)) {
                1
            } else if (o1.isType(TYPE_MOB) && o2.isType(TYPE_TURF)) {
                -1
            } else {
                1
            }
        }
    }
}
