package strongdmm.byond.dmm

inline class TileItemType(val value: String) {
    constructor(tileItem: TileItem) : this(tileItem.type)

    override fun toString(): String = value
}
