package strongdmm.byond.dmm

import strongdmm.util.inline.AbsPath

inline class MapId(val value: Int) {
    companion object {
        val NONE: MapId = MapId(-1)
    }

    constructor(absolutePath: AbsPath) : this(absolutePath.hashCode())
}
