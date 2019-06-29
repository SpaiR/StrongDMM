package io.github.spair.strongdmm.gui.objtree

import io.github.spair.strongdmm.common.TYPE_AREA
import io.github.spair.strongdmm.common.TYPE_MOB
import io.github.spair.strongdmm.common.TYPE_OBJ
import io.github.spair.strongdmm.common.TYPE_TURF

// /area -> /turf -> /obj -> /mob
object ObjectTreeNodeComparator : Comparator<ObjectTreeNode> {
    override fun compare(n1: ObjectTreeNode, n2: ObjectTreeNode): Int {
        val o1 = n1.type
        val o2 = n2.type

        if (o1.startsWith(TYPE_AREA) && o2.startsWith(TYPE_AREA)) {
            return o1.compareTo(o2)
        }
        if (o1.startsWith(TYPE_TURF) && o2.startsWith(TYPE_TURF)) {
            return o1.compareTo(o2)
        }
        if (o1.startsWith(TYPE_OBJ) && o2.startsWith(TYPE_OBJ)) {
            return o1.compareTo(o2)
        }
        if (o1.startsWith(TYPE_MOB) && o2.startsWith(TYPE_MOB)) {
            return o1.compareTo(o2)
        }

        return if (o1.startsWith(TYPE_AREA)) {
            -1
        } else if (o1.startsWith(TYPE_TURF) && o2.startsWith(TYPE_TURF)) {
            0
        } else if (o1.startsWith(TYPE_TURF) && (o2.startsWith(TYPE_OBJ) || o2.startsWith(TYPE_MOB))) {
            -1
        } else {
            if (o1.startsWith(TYPE_TURF) && o2.startsWith(TYPE_AREA)) {
                1
            } else if (o1.startsWith(TYPE_OBJ) && o2.startsWith(TYPE_MOB)) {
                -1
            } else {
                1
            }
        }
    }
}
