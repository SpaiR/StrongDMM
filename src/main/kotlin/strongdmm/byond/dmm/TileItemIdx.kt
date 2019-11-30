package strongdmm.byond.dmm

inline class TileItemIdx(val value: Int) {
    companion object {
        val AREA: TileItemIdx = TileItemIdx(-1)
        val TURF: TileItemIdx = TileItemIdx(-2)
    }

    override fun toString(): String = "$value"
}
