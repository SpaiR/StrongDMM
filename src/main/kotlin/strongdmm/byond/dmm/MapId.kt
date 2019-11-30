package strongdmm.byond.dmm

inline class MapId(val value: Int) {
    companion object {
        val NONE: MapId = MapId(-1)
    }

    constructor(absolutePath: String) : this(absolutePath.hashCode())
}
