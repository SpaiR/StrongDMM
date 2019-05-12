package io.github.spair.strongdmm.logic.render

import io.github.spair.strongdmm.logic.dme.TYPE_AREA
import io.github.spair.strongdmm.logic.dme.TYPE_MOB
import io.github.spair.strongdmm.logic.dme.TYPE_OBJ
import io.github.spair.strongdmm.logic.dme.TYPE_TURF

object RenderComparator : Comparator<RenderInstance> {

    private val RENDER_PRIORITY = arrayOf(TYPE_TURF, TYPE_OBJ, TYPE_MOB, TYPE_AREA)

    override fun compare(ri1: RenderInstance, ri2: RenderInstance): Int {
        val type1 = ri1.tileItem.type
        val type2 = ri2.tileItem.type

        for (type in RENDER_PRIORITY) {
            if (type1.startsWith(type) && !type2.startsWith(type)) {
                return -1
            } else if (!type1.startsWith(type) && type2.startsWith(type)) {
                return 1
            }
        }

        val plane1 = ri1.tileItem.plane
        val plane2 = ri2.tileItem.plane

        if (!plane1.equals(plane2)) {
            return plane1.compareTo(plane2)
        }

        val layer1 = ri1.tileItem.layer
        val layer2 = ri2.tileItem.layer

        return layer1.compareTo(layer2)
    }
}
