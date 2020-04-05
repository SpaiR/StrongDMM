package strongdmm.byond.dmm.parser

import strongdmm.byond.TYPE_AREA
import strongdmm.byond.TYPE_TURF

class TileObjectComparator : Comparator<TileObject> {
    override fun compare(obj1: TileObject, obj2: TileObject): Int {
        val type1 = obj1.type
        val type2 = obj2.type

        if (type1.startsWith(TYPE_AREA)) {
            if (!type2.startsWith(TYPE_AREA)) {
                return 1
            }
        } else if (type1.startsWith(TYPE_TURF)) {
            if (type2.startsWith(TYPE_AREA)) {
                return -1
            } else if (!type2.startsWith(TYPE_TURF)) {
                return 1
            }
        } else if (type2.startsWith(TYPE_TURF) || type2.startsWith(TYPE_AREA)) {
            return -1
        }

        return 0
    }
}
