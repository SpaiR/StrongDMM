package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dme.TYPE_MOB
import io.github.spair.strongdmm.logic.dme.TYPE_OBJ
import io.github.spair.strongdmm.logic.dme.TYPE_TURF
import io.github.spair.strongdmm.logic.map.TileItemProvider

object RenderComparator : Comparator<RenderInstance> {

    private val RENDER_PRIORITY = arrayOf(TYPE_TURF, TYPE_OBJ, TYPE_MOB, TYPE_AREA)

    override fun compare(ri1: RenderInstance, ri2: RenderInstance): Int {
        val tileItem1 = TileItemProvider.getByID(ri1.tileItemID)
        val tileItem2 = TileItemProvider.getByID(ri2.tileItemID)

        val type1 = tileItem1.type
        val type2 = tileItem2.type

        for (type in RENDER_PRIORITY) {
            if (type1.startsWith(type) && !type2.startsWith(type)) {
                return -1
            } else if (!type1.startsWith(type) && type2.startsWith(type)) {
                return 1
            }
        }

        val plane1 = tileItem1.plane
        val plane2 = tileItem2.plane

        if (!plane1.equals(plane2)) {
            return plane1.compareTo(plane2)
        }

        val layer1 = tileItem1.layer
        val layer2 = tileItem2.layer

        return layer1.compareTo(layer2)
    }
}
