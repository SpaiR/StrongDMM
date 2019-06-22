package io.github.spair.strongdmm.logic.map

import io.github.spair.strongdmm.common.TYPE_AREA
import io.github.spair.strongdmm.common.TYPE_MOB
import io.github.spair.strongdmm.common.TYPE_OBJ
import io.github.spair.strongdmm.common.TYPE_TURF

// area -> obj -> mob -> turf
object TileItemsComparator : Comparator<TileItem> {
    override fun compare(o1: TileItem, o2: TileItem): Int {
        return if (o1.isType(TYPE_AREA)) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_OBJ)) 0
        else if (o1.isType(TYPE_OBJ) && (o2.isType(TYPE_MOB) || o2.isType(
                TYPE_TURF
            ))) -1
        else if (o1.isType(TYPE_OBJ) && o2.isType(TYPE_AREA)) 1
        else if (o1.isType(TYPE_MOB) && o2.isType(TYPE_TURF)) -1
        else 1
    }
}
