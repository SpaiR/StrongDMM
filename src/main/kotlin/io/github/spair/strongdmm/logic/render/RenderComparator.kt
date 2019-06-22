package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.common.*
import io.github.spair.strongdmm.logic.map.TileItemProvider

object RenderComparator : Comparator<Long> {

    private val RENDER_PRIORITY = arrayOf(
        TYPE_TURF,
        TYPE_OBJ,
        TYPE_MOB,
        TYPE_AREA
    )

    override fun compare(riAddress1: Long, riAddress2: Long): Int {
        val tileItem1 = TileItemProvider.getByID(RenderInstanceStruct.getTileItemId(riAddress1))
        val tileItem2 = TileItemProvider.getByID(RenderInstanceStruct.getTileItemId(riAddress2))

        val type1 = tileItem1.type
        val type2 = tileItem2.type

        if (isType(type1, type2)) {
            return 0
        }

        for (type in RENDER_PRIORITY) {
            if (type1.startsWith(type) && !type2.startsWith(type)) {
                return -1
            } else if (!type1.startsWith(type) && type2.startsWith(type)) {
                return 1
            }
        }

        return 0
    }
}
