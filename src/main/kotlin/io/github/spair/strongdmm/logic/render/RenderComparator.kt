package io.github.spair.strongdmm.logic.render

import io.github.spair.byond.ByondTypes

object RenderComparator : Comparator<RenderInstance> {

    private val RENDER_PRIORITY = arrayOf(ByondTypes.TURF, ByondTypes.OBJ, ByondTypes.MOB, ByondTypes.AREA)

    override fun compare(ri1: RenderInstance, ri2: RenderInstance): Int {
        val type1 = ri1.type
        val type2 = ri2.type

        for (type in RENDER_PRIORITY) {
            if (type1.startsWith(type) && !type2.startsWith(type)) {
                return -1
            } else if (!type1.startsWith(type) && type2.startsWith(type)) {
                return 1
            }
        }

        val plane1 = ri1.plane
        val plane2 = ri2.plane

        if (!plane1.equals(plane2)) {
            return plane1.compareTo(plane2)
        }

        val layer1 = ri1.layer
        val layer2 = ri2.layer

        return layer1.compareTo(layer2)
    }
}
